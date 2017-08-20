# Mobile shared workspace

This is an application built for Android that allows users to share workspaces and text files between them.

The project includes both a solo (demo) version and the actual networked application.

The networked version allows file sharing between different devices, with offline mode and automatic reconciliation (using a simple distributed lock manager, based on Zookeeper's (https://zookeeper.apache.org/) implementation) once the devices are in range again.
This implementation has a dependency on Termite - a WifiDirect simulator (https://github.com/nuno-santos/termite).