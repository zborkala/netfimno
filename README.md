# Android NetFimno

__Android NetFimno is a java class which helps android developer to perform internet communication without any external third party libraries. NetFimno uses built-in android HttpUrlConnection.class to perform every kind of network requests and supports in all android versions.__

 ## What you can do with NetFimno are the following.
* Sending GET & POST requests.
* Uploading files and other data (optional) with progress indicators (like progressbar).
* Downloading files from internet with progress indicators. You can hide and show the downloading notification.

 ## All you need is

  * Add the following permissions in your AndroidManifest.xml
```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />

```
   * Include **NetFimno.java** and **Body.java** files in your package (com.example.yourapp)


 ## Sending GET Request.

 ```
 new NetFimno("https://example.com/something.php?id=3").get(new NetFimno.OnResult() {
                            @Override
                            public void onSuccess(String response) {
                             //Your code is here
                             // if(NetFimno.isJSON(response)) {}
                            }
                            }); 
 ```
 
 ## Sending POST Request.

```
   Body body = new Body()
                   .put("username", username)
                   .put("pwd", password)
                   .put("email", email);
                   
   new NetFimno("https://example.com/something.php").post(body.getMap(), new NetFimno.OnResult() {
                            @Override
                            public void onSuccess(String response) {
                             //Your code is here
                            }
                            }); 

```
 **Please note** `Body.put(String key,String value)` method has two parameters 
 
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
    new NetFimno(context,"https://example.com/something.php").multipart(files.getMap(), body.getMap(), new NetFimno.OnResultUpload() {
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
    new NetFimno(context,"https://example.com/something.php").multipart(files.getMap(), null, new NetFimno.OnResultUpload() {
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

 ```
     new NetFimno(context,"https://example.com/somfile.mp3")
     .setFileName("mySong.mp3")
     .setPath("audios")
     .setOnDownload(new NetFimno.OnDownload() {
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
 `.setFileName()`, `.setPath()` and `setDownload()` are optionals. You can use it or leave it like this.
 ```
 new NetFimno(context,"https://example.com/somfile.mp3").download(true);
``` 
 `.download(true)` to make notification visible and `.download(false)` is to make it invisible

  ## JSON Checker
  You can also check the response whether is in JSON format or not using static `NetFimno.isJSON(response)` method 
  `NetFimno.isJSON(response)` returns true if the response is in JSON format. Otherwise, it returns false.
  

 
