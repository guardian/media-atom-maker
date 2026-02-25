# Media Atom Maker Backfill sub-project

The Video Atom Backfiller is a standalone console application that can be used to make attribute changes across all
media atoms. It was first used to backfill new video dimensions on older videos.

It works by calling MAM's backend API to update and re-publish existing atoms that have missing data, emulating what the
frontend does.  For this reason, the backfiller needs to be supplied with cookie and csrf tokens from MAM running in the
browser.

### Important - backfiller affects the order of videos in the UI
> Be aware that when the backfiller changes video atoms, it will update the atom's last modified
date and therefore change the default ordering of atoms in the main MAM videos view.
>
> To minimize this effect, videos are processed in date order from oldest to newest.  Also, because
> CAPI only stores dates to the nearest second, the resulting order of atoms backfilled during the same second
> is effectively random.
>
> However, if some atoms are backfilled and other are not, the backfilled atoms will be brought to the top of
> the UI and non-backfilled atoms will be pushed down.
>
> So let Central Production know if you are doing something that will affect the appearance of atoms in the UI.
> A general workaround the get the atoms roughly in the 'expected' order is to switch the UI to order by created date.

## How to run it

First, for authentication you have to grab some Request Headers from the browser:
- Go to the chosen MAM environment e.g. https://video.gutools.co.uk or https://video.code.dev-gutools.co.uk
- Go into a video
- Open and clear the Network tab in the dev tools
- Make a minor change to the furniture headline or standfirst
- Observe the PUT request from apiRequest.ts in the Network tab
- In a terminal, export the Cookie and Csrf-Token request headers to environment variables
```bash
 export MAM_COOKIE="<paste-cookie-content>"
 export MAM_CSRF_TOKEN="<paste-csrf-token>"
```
> Note that you do not need Janus tokens because you are essentially borrowing MAM's web authentication.
> This means that you'll need permission to run MAM interactively to be able to run the backfiller.

Choose your backfill app - there are different ones for different attribute changes.  At the time of writing:
- `BackfillDimensions` which fills in the width and height and aspect ratios of videos.
- `BackfillPlatformAndVideoPlayerFormat` which fills in the atom-level platform attribute and video player format for self-hosted videos.

Run the app:
> Note that you can run the app and exit before making any changes

- In the same terminal, run your chosen backfill app:
```bash
 sbt "backfiller/runMain BackfillXXXXX"
```
- The app will prompt for the cookie and csrf token - press enter to use the env variables.
- It will then:
  - read all the atom id's up to a hard coded limit.
  - analyse each atom and print out what's missing if anything
  - make a list of update actions to be made
  - prompt to execute all actions, a specific number of actions, or exit.

> In PROD there are ~40,000 atoms and it can take a long time to run the backfiller analysis
> stage and even longer to execute changes, because it's calling the API for each atom.
>
> It's recommended to use the 'first N' option to build confidence.  Because actions are run in reverse chronological
> order, it's fine (if time-consuming) to execute in batches e.g. of 1000.

- When you choose to execute the actions it will:
  - update each atom
  - if the current version of the atom was published, it will republish to CAPI

### Error handling
On read operations the app will print an error message and continue.
Write operations fail and throw an exception which will terminate the app. Because actions are run in reverse chronological order, it makes sense to exit on error and then run again.


