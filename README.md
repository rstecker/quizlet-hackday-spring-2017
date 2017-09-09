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

- service clean up
  - OMG MAKE THINGS NOT BREAK ON LIFECYCLE CHANGES!!!!
  - just... ugh... clean everything up. Why is the model service different?
  - make bluetooth do a better job of maintaining connection...
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