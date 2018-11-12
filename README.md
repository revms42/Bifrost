# Bifrost
Bifrost is a model for communication that allows a fileset that exists in the cloud to be accessed and modified in a distributed way by seperate systems that have a copy of that fileset.
One aspect of Bifrost that other, related, concepts like version management don't have is a concept of a single concurrent "owner" to the data. In other words, if one user has checked out the data for edit then another user would not be able to check out, or in, any changes till the first user has completed his operations.

The reason for this is, at it's heart, Bifrost is being designed to facilitate the offline storing of game server files, where, inherently, one user would be hosting the servered game at any one time. Conceptually, once the server is running other players would need to know how to connect to that server, but access to the data regarding the server itself would be locked by the user hosting the server until such time as they have shut the server down (at which point the current state would be saved to the cloud).

In order to facilitate this, and despite being modeled in markup, the messages are designed to be "terse". In other words, the messages sent contain the minimum amount of information needed to fullfill the request. The idea is to create a communication model that would allow the dataset(s) to be stored remotely in the cloud and have a extremely lightweight server be the gateway to information regarding that dataset (without actually hosting the data itself) so that it could be run in the lowest (e.g. free) tiers of cloud based web hosting services without risking incurring additional monthly payments.

## Getting Started
Use Intellij's import from GitHub option and let it recreate stuff that isn't there (should take you through the gradle setup stuff).