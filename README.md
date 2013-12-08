# ab2podcast

Transforms a directory of AudioBooks into a series of podcasts.    Good for using an podcast player (like CarCast)
to listen to audiobooks

## Getting Started

1. rip some Audio Books using a ripping program (say itunes)
2. Start the application: `lein run`
3. Go to [localhost:8080](http://localhost:8080/) to see the podcasts

## Configuration

### Ubuntu 

On Ubuntu the ~/Music directory is expected to have the audio books in Author/Title/*.mp3 format

For example, I use this shell script (saved as ~/rip.sh)

```
#!/bin/bash
# A simple shell script to rip audio cd and create mp3 using lame 
# and cdparanoia utilities.
# ----------------------------------------------------------------------------
# Written by Vivek Gite <http://www.cyberciti.biz/>
# (c) 2006 nixCraft under GNU GPL v2.0+
# ----------------------------------------------------------------------------
read -p "Starting in 5 seconds ( to abort press CTRL + C ) " -t 5
cdparanoia -B
for i in *.wav
do
	lame --vbr-new -b 360 "$i" "${i%%.cdda.wav}.mp3"
	rm -f "$i"
done
```

like so,
```
  $ mkdir -p album/disk1
  $ cd album/disk1
  $ # load first disk
  $ ~/rip.sh
  $ mkdir ../disk2
  $ cd ../disk2
  $ # load second disk
  $ ~/rip.sh
...
```

## OSX

On OSX the default itunes home directory is used.  So simply rip from itunes (configured to rip as MP3.)

