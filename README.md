# Bifrost
### Current Version 0.0.1-SNAPSHOT

Bifrost is a model for communication that allows a fileset that exists in the cloud to be accessed and modified in a distributed way by seperate systems that have a copy of that fileset.
One aspect of Bifrost that other, related, concepts like version management don't have is a concept of a single concurrent "owner" to the data. In other words, if one user has checked out the data for edit then another user would not be able to check out, or in, any changes till the first user has completed his operations.

The reason for this is, at it's heart, Bifrost is being designed to facilitate the offline storing of game server files, where, inherently, one user would be hosting the servered game at any one time. Conceptually, once the server is running other players would need to know how to connect to that server, but access to the data regarding the server itself would be locked by the user hosting the server until such time as they have shut the server down (at which point the current state would be saved to the cloud).

In order to facilitate this, and despite being modeled in markup, the messages are designed to be "terse". In other words, the messages sent contain the minimum amount of information needed to fullfill the request. The idea is to create a communication model that would allow the dataset(s) to be stored remotely in the cloud and have a extremely lightweight server be the gateway to information regarding that dataset (without actually hosting the data itself) so that it could be run in the lowest (e.g. free) tiers of cloud based web hosting services without risking incurring additional monthly payments.

## Getting Started
After cloning the repo, open up a recent Spring Tool Suite and create a new workspace (if you're not familiar with the way that Eclipse works, this shouldn't be a workspace on top of the repo, put it somewhere else).
Starting with the BifrostCore project import the projects from the file system (core, server, then client).

## Features in Latest Snapshot
### Server Features
- Server tracks versions of files and their location allowing clients to checkout and checkin the mappings.
- Server will grab the IP address and associate it with a supplied port to notify other users where the game is being hosted.
- Communication protocol is terse, supplying only the bare minimum in information required for the client to do it's job.
- Server is very small and lightweight.

### Client Features
- Heimdall client maintains properties and inventory files necessary to monitor and track mappings to a server.
- For "happy path" scenarios Heimdall can handle the anticipated workflows:
	1. Create a new mapping from the file system and upload it to storage and make an entry in the Bifrost server.
	2. Identify an update on the Bifrost server, download the update, and checkout the mapping.
	3. Identify that the current version of a mapping is newer than the existing server mapping, and update the server.
	4. Download a new mapping from the server, update local files, and check it out on the server.
	5. Check out an existing, up to date, mapping from the server.
	6. Check in a previously checked out mapping.
- Heimdall will persist checked mapping information for checked out mappings even if it is terminated.
- Heimdall will recognize that it has something checked out when it is started and can resume monitoring.
- Heimdall will let you select your properties and inventory, but will create a default of both if they are not present.
- Heimdall has ui for changing the targetted server, the upload plugin properties, and the additional properties that it uses to run.
- Currently there are three uploader plugins; dropbox, string, and local file system. Only dropbox has been tested so far.

## Development List
- [ ] Uploader plugin for OneDrive.
- [ ] Uploader plugin for FTP.
- [ ] Metadata for mappings (e.g. this is a minecraft mapping).
- [ ] Ability to filter based on metadata.
- [ ] Investigate security for checkout/checkin (right now it's first come first served).
- [ ] Create FTB compatible plugin for Bifrost (e.g. a minecraft plugin client).
- [ ] Create a script for DwarfFortress as a client.
- [ ] Investigate other games to integrate with Bifrost.
- [ ] Create documentation for Bifrost development.