import { Client, type IMessage } from '@stomp/stompjs'
import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { websocketUrl } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export function RealtimeSync() {
  const queryClient = useQueryClient()
  const { user } = useAuth()

  useEffect(() => {
    if (!user) return
    let connectedOnce = false
    const client = new Client({
      brokerURL: websocketUrl(),
      reconnectDelay: 5000,
      onConnect: () => {
        if (connectedOnce) queryClient.invalidateQueries()
        connectedOnce = true
        client.subscribe('/topic/issues', (_message: IMessage) => {
          queryClient.invalidateQueries({ queryKey: ['issues'] })
          queryClient.invalidateQueries({ queryKey: ['my-issues'] })
          queryClient.invalidateQueries({ queryKey: ['analytics'] })
          queryClient.invalidateQueries({ queryKey: ['issue'] })
        })
      },
    })
    client.activate()
    return () => { void client.deactivate() }
  }, [queryClient, user])

  return null
}
