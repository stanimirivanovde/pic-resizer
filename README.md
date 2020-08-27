# pic-resizer
Resize pictures to fit as email attachments. This was a project I did for my mom long time ago when I was learning Java. The interface is in Bulgarian. It crafts an imagemagick command to convert a picture. It can use multiple threads to execute the conversion.

# Usage
Make sure you update the file config.properties to supply the path to the convert tool from the imagemagick package:
```properties
convertPath=/usr/local/bin/convert
destinationPath=mypics/
quality=85
size=1280x1024
numberOfThreads=5
```

Then run the app
```
java PicResizer
```
Typical output is as follows:
```
Working on file /Users/stanimir/Desktop/Screen Shot 2020-08-18 at 20.14.58.png
Saving file to mypics/Screen Shot 2020-08-18 at 20.14.58.jpg
Executing the command: /usr/local/bin/convert /Users/stanimir/Desktop/Screen Shot 2020-08-18 at 20.14.58.png -resize 1280x1024 -quality 85 mypics/Screen Shot 2020-08-18 at 20.14.58.jpg
```
