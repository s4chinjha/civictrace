# Explorer MVP Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Complete the public Explorer MVP with server-backed pagination and filters for every supported issue status.

**Architecture:** Reuse the existing paginated `GET /api/issues` contract. The React Explorer will own the current page, include it in the React Query key, and render accessible previous/next controls while preserving the existing list/map toggle.

**Tech Stack:** Spring Boot 3.5, Spring Data JPA, React 19, TypeScript, TanStack Query, Vitest, Testing Library.

**Spec:** Existing CivicTrace MVP requirements and current API contract in `backend/src/main/java/com/civictrace/issue/IssueResponses.java`.

## Global Constraints

- Keep the modular monolith architecture; do not add services, Redis, Kafka, or paid APIs.
- Preserve public read access to issue browsing and existing resident/staff authorization.
- Use the existing `IssuePage` response (`content`, `page`, `size`, `totalElements`, `totalPages`).
- Do not fetch an arbitrary 100-record limit from the frontend.
- Keep all six backend statuses available in the Explorer filter.

## Review Focus

- Changing a filter must reset pagination to page one.
- A page boundary must disable the corresponding navigation control.
- A result set with zero records must still show accurate pagination/count state.
- Map view must display only the currently loaded page, matching list view semantics.
- Search, category, and status values must remain encoded through `URLSearchParams`.

### Task 1: Explorer pagination and complete status filters

**Files:**
- Modify: `frontend/src/pages/ExplorerPage.tsx`
- Modify: `frontend/src/styles.css`
- Test: `frontend/src/pages/ExplorerPage.test.tsx`

**Interfaces:**
- Consumes: `IssuePage` from `frontend/src/types.ts` and `apiRequest` from `frontend/src/api/client.ts`.
- Produces: paginated Explorer requests using zero-based `page` and a stable page size of 20.

- [ ] Write tests proving all statuses render, changing a filter requests `page=0`, next-page navigation requests the next page, and previous/next controls disable at boundaries.
- [ ] Run the focused test and verify it fails because the current Explorer requests `size=100` and has no pagination controls.
- [ ] Add `page` state, use `page=0&size=20` in the query, include page in the query key, and reset page whenever search/category/status changes.
- [ ] Add `REJECTED` and `DUPLICATE` filter options.
- [ ] Render an accessible pagination toolbar with current page/total pages, result range, and disabled boundary buttons; keep map data tied to the current page.
- [ ] Add focused responsive styles consistent with the existing controls.
- [ ] Run the focused frontend test and verify it passes.

### Task 2: Regression verification and documentation

**Files:**
- Modify: `README.md` if the Explorer behavior is described or the old 100-record limitation is present.

- [ ] Run the full frontend test suite and production build.
- [ ] Run the full backend test suite to confirm the unchanged API contract remains green.
- [ ] Inspect `git diff`, confirm no secrets or unrelated files changed, and commit the completed MVP improvement.
