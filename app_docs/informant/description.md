Local workspace for researching YouTube videos, articles, and Reddit posts. Organize work into projects, keep notes and bookmarks on each item, and run chat or analysis against the material you collected. No YouTube account login — API keys go in Settings.

## Features
- Project-based workspace: create, search, sort, rename, export, and delete projects
- Import YouTube, article, and Reddit URLs (single links or mixed batches)
- Android share-target: share a link into INFORMANT to start or fill a project
- Per-item tabs for notes, bookmarks, transcripts, comments, chat, and custom AI actions
- Quote bookmarks in articles/Reddit; timestamp bookmarks on videos with jump-to
- Fetch and search YouTube captions; click a line to seek the player
- Clickable timestamp citations in chat and AI outputs (jump back into the video)
- Pull YouTube or Reddit comment threads, search/filter, and favorite comments
- Project-wide chat and AI actions across selected items, with a Texts tab for context
- One floating YouTube player (inline, expanded, or PiP) with saved playback position
- From expanded view, PiP fullscreenes the tabs so you can keep reading while the mini-player plays
- Export/import project JSON with selectable artifacts
- External search from highlighted text (Perplexity, Google, ChatGPT, and more)

## Content & tools
Open a card for tabs that match the content type:
- **YouTube** — Info, Comments, Transcript, Notes, Bookmarks, Chat / AI actions
- **Articles** — Reader, Notes, Bookmarks, Chat / AI actions
- **Reddit** — Post reader, Comments, Notes, Bookmarks, Chat / AI actions

Built-in AI actions include summary, analysis, key takeaways, and lists. You can add your own. Outputs can include mind maps when relevant.

## Platforms
- **Android** — Capacitor build; IndexedDB on device; native share import
- **Desktop** — Windows portable / local server; projects as JSON; artifacts in SQLite (`informant.db`)

## Requirements
Optional keys in Settings (or `.env` on desktop): Gemini, YouTube Data API, YouTube Transcript API, Webshare for scraping/transcripts.
