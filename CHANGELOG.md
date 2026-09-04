# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.0.1] - Unreleased

### Added

- **NDEx v3 folder support** — `getMyFolders(int limit)` lists the signed-in user's folders via `GET /v3/files/folders`, and `moveNetworks(MoveNetworksRequest)` moves networks into a folder via `POST /v3/batch/networks/move`.
- **NDEx v3 file search** — `searchFiles(SimpleFileQuery, FileVisibilityType, int start, int size)` queries `POST /v3/search/files`, supporting an optional `PUBLIC`/`PRIVATE` visibility filter and paging, and returning a `FileSearchResult`.
- **Visibility and folder on CX2 network create** — `createCX2Network(InputStream, VisibilityType, UUID folderId)` adds the `visibility` and `folderId` query parameters to `POST /v3/networks`, so a network can be created directly into a folder with its visibility set. A null value omits its parameter.
- **Visibility on CX2 network update** — `updateCX2Network(UUID, InputStream, VisibilityType)` adds the `visibility` query parameter to `PUT /v3/networks/{networkid}`. A null value omits it.

The pre-existing single-argument `createCX2Network(InputStream)` and `updateCX2Network(UUID, InputStream)` now delegate to the new overloads with null arguments and produce exactly the same unqueried routes as before, so existing callers are unaffected.

### Removed

- **Travis CI configuration** — deleted `.travis.yml` and `.travis.settings.xml` and dropped the Travis build-status badge from `README.md`. Replaced by GitHub Actions `ci.yml` and `release.yml` workflows, the latter deploying to the NRNB Nexus repository on a published GitHub release.
