# quizlet-hackday-spring-2017
It's June 2017 and time for the Q2 Hackday here at Quizlet

... and am also going for round 2 in Q3 2017 Hack Day...

# Setup

Currently there are a couple strings required in the `local.properties` file:

- `quizletClientId` : Quizlet Client ID, found on [this API docs page](https://quizlet.com/api-dashboard)
- `quizletEncodedString` : the already-encoded basic authorization string. See table halfway down page in [Step 2 section of this page](https://quizlet.com/api/2.0/docs/authorization-code-flow)
- `quizletRedirectUrl` : Quizlet redirect URI. Would recommend something like `http://localhost:8080`. URI is used for auth, the page is not actually visited. Must match what you've set on [your API dashboard](https://quizlet.com/api-dashboard)
- `quizletSecretCode` : a random string. TODO : I missunderstood originally. This should be pulled out and generated randomly.... 

# TODOs

- service/bluetooth clean up
  - OMG MAKE THINGS NOT BREAK ON LIFECYCLE CHANGES!!!!
  - just... ugh... clean everything up. Why is the model service different?
  - make bluetooth do a better job of maintaining connection...
  - kick players from the game when they drop off
  - know when you've lost your connection to the host
- clean up Auth flow
  - expose errors to users
  - refactor the secret code to be randomly generated
  - fix the popup dialog
- clean up app start page
  - do a better job about asking for permissions
  - clean up UI
- clean up Set selection page
  - polish UI
  - move broadcast request to lobby
- clean up Game selection page
  - imrpove messaging about WAITING for game to start
  - clean up UI
- clean up Lobby page
  - omg, UI fix
  - move broadcast request here for hosts
  - display set details
  - allow game type selection
- clean up Board page
  - UI cleanup
  - show stats/state
  - show question feedback
- implement end game logic
  - end game state screen
  - logic in game engine

# Notes on Game Engine 

How to pipe data from the Host to a Player

1. ... start somewhere in host logic...
2. add it to the `QCGameMessage` object. That's what gets piped over via Bluetooth
3. pick it out of the `QCGameMessage` in the `PlayerEngine` 
4. depending on what phase of the game it is, pass through the details via `LobbyState` or `BoardState`
5. modify how the states are processed in the `TopLevelViewModel` (piped through automatically by the `StartActivity`), which will require you to modify the various DB objects as needed to pass changes through via `LiveData` to the UI Fragments
6. in your Fragments, listen to the correct `LiveData` feed.

See, easy!  While it may look tedious, it's great because we have 3 different models to seperate the 3 different levels of concern : bluetooth, game logic, ui logic.  Also, lets be clear-- you really shouldn't be piping brand new data formats from host to player all that often. Doing so is a breaking schema change and we really should have though things out before hand. It's expected that most changes are isolated to just game logic OR just ui logic and can fit into the communication protocall already...



 (note : this doc was written in Sublime, without the safety net of spell check)