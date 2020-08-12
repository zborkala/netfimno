package com.fimno.myshop.helper;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class NetHelper {
    String baseUrl;
    Context context;
    int uploaded = 0;
    int fileLength = 0;
    OnResultUpload onResultUpload;
    String fileName;
    String path = "./";
    int dl_progress;
    OnDownload onDownload;

    public NetHelper(Context context, String url) {
        this.context = context;
        baseUrl = url;
    }

    public NetHelper(String url) {
        baseUrl = url;
    }

    public void download(boolean notificationVisibility){
        if(fileName == null) {
            fileName = baseUrl.substring(baseUrl.lastIndexOf("/")+1);
        }
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(baseUrl));
        if(notificationVisibility) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        } else {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        }
        request.setDestinationInExternalFilesDir(context, path, fileName);
        final long did = manager.enqueue(request);
        if(onDownload != null) {
            setProgress(manager, did);
        }
    }


    public void get(final OnResult onResult) {
      new Thread(new Runnable() {
          @Override
          public void run() {
              HttpURLConnection urlConnection = null;
              try {
                  URL url = new URL(baseUrl);
                  urlConnection = (HttpURLConnection) url.openConnection();
                  urlConnection.setRequestMethod("GET");
                  urlConnection.setDoOutput(true);
                  urlConnection.setDoInput(true);
                  urlConnection.setUseCaches(false);
                  InputStream in = urlConnection.getInputStream();
                  streamToString(in,onResult);
              } catch (MalformedURLException e) {
                  e.printStackTrace();
              } catch (IOException e) {
                  e.printStackTrace();
              } catch (Exception e) {
                  e.printStackTrace();
              } finally {
                  assert urlConnection != null;
                  urlConnection.disconnect();
              }
          }
      }).start();
    }

    public void post(final Map<String,String> params, final OnResult onResult){
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(baseUrl);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    OutputStream outputStream = connection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = "";
                    for (String key : params.keySet()) {
                        String value = params.get(key);
                        post_data += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") + "&";
                    }
                    bufferedWriter.write(post_data.substring(0,post_data.length()-1));
                    bufferedWriter.flush();
                    inputStream = connection.getInputStream();
                    streamToString(inputStream,onResult);
                    bufferedWriter.close();
                    outputStream.close();
                    inputStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();

    }


    public static boolean isJSON(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException e) {
            try {
                new JSONArray(test);
            } catch (JSONException e1) {
                return false;
            }
        }
        return true;
    }


    public void multipart(final Map<String,String> fieldAndPath, final Map<String, String> params, final OnResultUpload onResult) {
        RequestBody body = new RequestBody(fieldAndPath, params);
        body.getBody(new OnLengthCalc() {
            @Override
            public void getLength(final int length) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onResultUpload = onResult;
                        HttpURLConnection connection = null;
                        DataOutputStream outputStream;
                        InputStream inputStream;
                        String twoHyphens = "--";
                        String boundary = "*****" + System.currentTimeMillis() + "*****";
                        String lineEnd = "\r\n";

                        int bytesRead;
                        byte[] buffer;


                        try {


                            URL url = new URL(baseUrl);
                            connection = (HttpURLConnection) url.openConnection();

                            connection.setDoInput(true);
                            connection.setDoOutput(true);
                            connection.setUseCaches(false);

                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Connection", "Keep-Alive");
                            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                            connection.setRequestProperty("Content-Length", ""
                                    + length);
                            connection.setFixedLengthStreamingMode(length);
                            connection.connect();
                            outputStream = new DataOutputStream(connection.getOutputStream());

                            if (fieldAndPath != null) {
                                for (String fileField : fieldAndPath.keySet()) {
                                    String filepath = fieldAndPath.get(fileField);
                                    File file = new File(filepath);
                                    fileLength = (int) file.length();
                                    FileInputStream fileInputStream = new FileInputStream(file);
                                    String[] q = filepath.split("/");
                                    int idx = q.length - 1;
                                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                                    outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
                                    outputStream.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                                    outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                                    outputStream.writeBytes(lineEnd);
                                    outputStream.flush();
                                    Activity activity = (Activity) context;
                                    buffer = new byte[1024];

                                    BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file));
                                    long time = System.currentTimeMillis() - 200;
                                    while ((bytesRead = bufInput.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, bytesRead);
                                        outputStream.flush();
                                        uploaded += bytesRead;
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                int percent1 = Math.round((float) uploaded * 100 / length);
                                                onResultUpload.getProgress(percent1);
                                            }
                                        });

                                    }
                                    outputStream.writeBytes(lineEnd);
                                    outputStream.flush();
                                    fileInputStream.close();

                                }
                            }

                            // Upload POST Data
                            if (params != null) {
                                for (String key : params.keySet()) {
                                    String value = params.get(key);
                                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                                    outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                                    outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                                    outputStream.writeBytes(lineEnd);
                                    outputStream.writeBytes(value);
                                    outputStream.writeBytes(lineEnd);
                                    outputStream.flush();
                                }
                            }
                            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                            inputStream = connection.getInputStream();
                            streamToString(inputStream,onResult);

                            inputStream.close();
                            outputStream.flush();
                            outputStream.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            //Log.d("",e.toString()+"");
                        } finally {
                            if (connection != null) {
                                connection.disconnect();
                            }
                        }
                    }
                }).start();


            }
        });
    }
    private void setProgress(final DownloadManager manager, final long did){
        if(dl_progress < 100){
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(did);
                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    cursor.close();
                    dl_progress = (int) ((bytes_downloaded * 1f / bytes_total) * 100);
                    onDownload.progress(dl_progress);
                    setProgress(manager,did);
                }
            }, 50);
        } else {
            onDownload.complete();
        }
    }
    private void streamToString(InputStream is, final OnResult result) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    result.onSuccess(sb.toString());
                }
            },200);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public NetHelper setOnDownload(OnDownload onDownload) {
        this.onDownload = onDownload;
        return this;
    }

    public NetHelper setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public NetHelper setPath(String path) {
        this.path = path;
        return this;
    }

    private void streamToString(InputStream is, final OnResultUpload result) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    result.onSuccess(sb.toString());
                }
            },200);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // RequestBody Class

    static class RequestBody {
        String twoHyphens = "--";
        String boundary = "*****" + System.currentTimeMillis() + "*****";
        String lineEnd = "\r\n";
        Map<String,String> fieldAndPath;
        Map<String, String> params;
        int length = 0;
        String data;

        public RequestBody(Map<String,String> fieldAndPath, Map<String, String> params) {
            this.fieldAndPath = fieldAndPath;
            this.params = params;
        }

        public void getBody(OnLengthCalc onLengthCalc) {
            data = "";
            if (fieldAndPath != null) {
                for (String fileField : fieldAndPath.keySet()) {
                    String filepath = fieldAndPath.get(fileField);
                    assert filepath != null;
                    String[] q = filepath.split("/");
                    int idx = q.length - 1;
                    File file = new File(filepath);
                    length += (int) file.length();
                    data += twoHyphens + boundary + lineEnd;
                    data += "Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + q[idx] + "\"" + lineEnd;
                    data += "Content-Type: application/octet-stream" + lineEnd;
                    data += "Content-Transfer-Encoding: binary" + lineEnd + lineEnd + lineEnd;
                }
            }
            // Upload POST Data
            for (String key : params.keySet()) {
                String value = params.get(key);
                data += twoHyphens + boundary + lineEnd;
                data += "Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd;
                data += "Content-Type: text/plain" + lineEnd;
                data += lineEnd;
                data += value;
                data += lineEnd;
            }

            data += twoHyphens + boundary + twoHyphens + lineEnd;
            length += data.length();
            onLengthCalc.getLength(length);

        }

    }

    //Public Interfaces
    public interface OnResult {
        void onSuccess(String response);
    }
    public interface OnResultUpload {
        void onSuccess(String response);
        void getProgress(int progress);
    }
    public interface OnDownload {
        void progress(int percent);
        void complete();
    }
    public interface OnLengthCalc {
        void getLength(int length);
    }
}
