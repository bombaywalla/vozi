# Change Log
All notable changes to this project will be documented in this file.
This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Changed
- Changed the `install` and `deploy` aliases to work with `-X` rather
  than `-M`.

## [0.1.6] - 2021-01-06
### Added
- Added more examples to intro.md.

## [0.1.5] - 2021-01-03
### Changed
- Fixed typos in the intro.md doc.
- Fixed issues reported by clj-kondo.
- Updated deps.
- Changed aliases (esp for depstar) and documented in README.
- Updated docstrings and comments.
- Refactored asserts to print better error messages.
- Updated the intro doc to use a smaller example.
- `opts` to the `base-plot` can override the `:mark-type`.
- Updated copyright year.

### Added
- Added tests to check assert errors on bad input.
- If the parameter to `add-data` is a string, treat as a URL.
- Added `density-transform`.
- Added `plot-opts` to `base-plot`.
- Added `density-opts` to `density-plot`.
- Marked non-API functions with `^:private`.
- Addded a `config` function.

## [0.1.4] - 2020-09-07
### Changed
- Changed License in pom from Eclipse to MIT. It was always MIT in the
  LICENSE file.
- Updated the docstrings to be more informative and mark those
  functions that are not part of the API.

### Added
- Added an usage example to the docs.

## [0.1.3] - 2020-08-24
### Changed
- Improved the facet function to handle a bare faceted field in
  addition to the embedded row and column maps.

## [0.1.2] - 2020-08-17
### Added
- Added joinaggregate-transform.
- Added Clojars and Cljdoc badges.

## [0.1.1] - 2020-08-16
### Changed
- Moved the logging deps to dev.

### Removed
- Removed the integrant and aero deps and uses.

### Added
- Added a quickstart example to the README.

## 0.1.0 - 2020-08-15
### Added
- First cut, taken from rwri/viz.

[Unreleased]: https://github.com/bombaywalla/vozi/compare/v0.1.6...HEAD
[0.1.6]: https://github.com/bombaywalla/vozi/compare/v0.1.5...v0.1.6
[0.1.5]: https://github.com/bombaywalla/vozi/compare/v0.1.4...v0.1.5
[0.1.4]: https://github.com/bombaywalla/vozi/compare/v0.1.3...v0.1.4
[0.1.3]: https://github.com/bombaywalla/vozi/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/bombaywalla/vozi/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/bombaywalla/vozi/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/bombaywalla/vozi/releases/tag/v0.1.0
