# Android NetHelper

__Android NetHelper is a java class which helps android developer to perform internet communication without any external third party libraries. NetHelper uses built-in android HttpUrlConnection.class to perform ever kind of network requests. __

 ## What you can do with NetHelper are the following.
* Sending GET & POST requests.
* Uploading files and other data with progress indicators (like progressbar).
* Downloading files from internet with progress indicators. You can hide and show the downloading notification.

 ## All you need is

  * Add the following permission in your AndroidManifest.xml
```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />

```
   * Include **NetHelper.java** and **Body.java** files in your package (com.example.yourapp)


 ## Sending GET Request.

 ```
 new NetHelper("https://example.com/something.php?id=3").get(new NetHelper.OnResult() {
                            @Override
                            public void onSuccess(String response) {
                             //Your code is here
                            }
                            }); 
 ```
 
 ## Sending POST Request.

```
   Body body = new Body()
                   .put("username", username)
                   .put("pwd", password)
                   .put("email", email);
                   
   new NetHelper("https://example.com/something.php").post(body.getMap(), new NetHelper.OnResult() {
                            @Override
                            public void onSuccess(String response) {
                             //Your code is here
                            }
                            }); 

```
 **Please note** Body.put() method has two parameters (String key,String value)
 
 ## Uploading files with other data
 
 ```
    Body body = new Body()
                   .put("username", "Zikkoo")
                   .put("pwd", "8styadf")
                   .put("email", "example@gmail.com");
                   
    //You can upload more than one file, here I am uploading two files   
    Body files = new Body()
                     .put("file1", "filePath1")
                     .put("file2", "filePath2");
    new NetHelper(context,"https://example.com/something.php").multipart(files.getMap(), body.getMap(), new NetHelper.OnResultUpload() {
                            @Override
                            public void onSuccess(String response) {   
                               //Your code comes here
                            }

                            @Override
                            public void getProgress(int progress) {
                                // Progress is always in percentage
                                // myProgressBar.setProgress(progress);
                            }
                        });
 ```
 
  ## Uploading files with no other data
  
   ``` 
    Body files = new Body()
                     .put("file1", "filePath1")
                     .put("file2", "filePath2");
    new NetHelper(context,"https://example.com/something.php").multipart(files.getMap(), null, new NetHelper.OnResultUpload() {
                            @Override
                            public void onSuccess(String response) {   
                               //Your code comes here
                            }

                            @Override
                            public void getProgress(int progress) {
                                // Progress is always in percentage
                                // myProgressBar.setProgress(progress);
                            }
                        });
 ```
  ## Downloading a file from the server 
 void progress(int percent);
        void complete(); 
 ```
     new NetHelper(context,"https://example.com/somfile.mp3")
     .setFileName("mySong.mp3")
     .setPath("audios")
     .setOnDownload(new NetHelper.OnDownload() {
          @Override
          public void progress(int percent) {   
              // Progress is always in percentage
              // myProgressBar.setProgress(progress);            
          }

          @Override
          public void complete() {
             //Your code comes here
          }
     }).download(true);
 ```
 .setFileName(), .setPath() and setDownload() are optionals. You can use it or leave it like this.
 ```
 new NetHelper(context,"https://example.com/somfile.mp3").download(true);
``` 
 .download(true) to make notification visible and .download(false) is to make it invisible
