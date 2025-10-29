# Qilletni IntelliJ Plugin

This is an IntelliJ plugin for the [Qilletni](https://qilletni.dev/) language, which is a DSL providing first-class support for music streaming/tracking services. This language currently supports Spotify, Last.Fm, and Tidal is in development.

This plugin primarily does the following:
- Syntax highlighting
- Error checking
- Auto completions for Spotify tracks, playlists, and artists (More services planned)
- References/highlight usages/go-to's in Qilletni
- Inspections for adding music
- Building and execution of programs
- Library indexing
- Linking between qilletni_info.yml native Java methods references
- Documentation rendering

As a warning, unlike the rest of Qilletni/its ecosystem, this plugin was written ~80% with various AI tools. This was both to experiment with tools, and because I really dislike writing IDEA plugins.

## Screenshots

<img src="screenshots/qilletni_info_refs.png" alt="qilletni_info.yml linking to their actual Java classes" width="500">

_qilletni_info.yml linking to their actual Java classes_

<br/>

![](screenshots/ql_native_linking.png)
![](screenshots/java_native_linking.png)

_Native Qilletni functions are linked to their Java implementations. Clicking on either will _

<br/>

<img src="screenshots/doc_rendering.png" alt="Rendering the documentation for redirectPlayToList" width="500">

_Rendering the documentation for redirectPlayToList_

<br/>

<img src="screenshots/music_autocomplete.png" alt="Auto completing God Knows by Knocked Loose in a song variable" width="600">
<br/>
<img src="screenshots/music_autocomplete_after.png" alt="After auto completing is done" width="400">

_Before and after autocomplete of music information. This is context-aware of what is being completed._


<img src="screenshots/music_selector_popup.png" alt="Music selector popup" width="600">

_The magnifier glass selector for music, providing a GUI-based selector for songs, tracks, or playlists_
