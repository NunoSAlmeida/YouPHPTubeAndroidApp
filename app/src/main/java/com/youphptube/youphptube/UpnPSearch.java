package com.youphptube.youphptube;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import net.mm2d.upnp.Action;
import net.mm2d.upnp.ControlPoint;
import net.mm2d.upnp.Device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public class UpnPSearch extends AppCompatActivity {
    MasterClass.VideoPlaying CurrentVideo = new MasterClass.VideoPlaying();
    ArrayList<Device> DetectedDevices = new ArrayList<>();
    ArrayList<String> DevicesNames = new ArrayList<>();
    ListView DevicesList;
    final ControlPoint cp = new ControlPoint();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.hasExtra("VideoInformation")){
            CurrentVideo = (MasterClass.VideoPlaying) intent.getExtras().getSerializable("VideoInformation");
        }


        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_upn_psearch);

        DevicesList = findViewById(R.id.ListaSelectAction);

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, DevicesNames);

        DevicesList.setAdapter(itemsAdapter);
        DevicesList.setEmptyView(findViewById(R.id.emptyElement));
        DevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                      @Override
                                      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                          view.setBackgroundColor(getResources().getColor(R.color.onclicklist));
                                          VideoPlayer.UpnPDevice = DetectedDevices.get(i);
                                          PlayOnDevice(DetectedDevices.get(i));
                                      }
                                  });


                cp.initialize();
        // adding listener if necessary.
        cp.addDiscoveryListener(new ControlPoint.DiscoveryListener() {
            @Override
            public void onDiscover(@Nonnull Device device) {
                Action accao= device.findAction("SetAVTransportURI");
                if (accao != null){
                    DetectedDevices.add(device);
                    DevicesNames.add(device.getFriendlyName());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((BaseAdapter) DevicesList.getAdapter()).notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onLost(@Nonnull Device device) {
                /*final Device DeviceInfo = device;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(UpnPSearch.this, "Lost " + DeviceInfo.getFriendlyName(), Toast.LENGTH_LONG).show();
                    }
                });*/
            }
        });
        cp.start();
        //cp.search("ST: urn:schemas-upnp-org:service:AVTransport:1");
        cp.search();


    }


    void PlayOnDevice(final Device DeviceInfo){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Device mediaServer = cp.getDevice(DeviceInfo.getUdn());
                    //Stop(device);
                    //Lets stop any previous video playing
                    Action Stop = DeviceInfo.findAction("Stop");
                    Map<String, String> StopCommands = new HashMap<>();
                    StopCommands.put("InstanceID", "0");
                    //If there is no video player it will give error
                    try{
                        Stop.invoke(StopCommands);
                    }catch (NullPointerException e){

                    }catch (Exception e){

                    }

                    Thread.sleep(100);

                    Action SetVideo = DeviceInfo.findAction("SetAVTransportURI");
                    Map<String, String> SetVideoCommands = new HashMap<>();
                    SetVideoCommands.put("InstanceID", "0");
                    SetVideoCommands.put("CurrentURI", CurrentVideo.VideoURI);
                    SetVideoCommands.put("CurrentURIMetaData", "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
                                "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
                                "xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
                                "<item id=\"0\" parentID=\"0\" restricted=\"1\">" +
                                "<dc:title>" + CurrentVideo.VideoTitle + "</dc:title>" +
                                "<upnp:class>object.item.videoItem</upnp:class>" +
                                "<res protocolInfo=\"http-get:*:video/mp4:DLNA.ORG_PN=AVC_MP4_BL_L3L_SD_AAC;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=61700000000000000000000000000000\" duration=\"" + CurrentVideo.VideoDuration + "\" sec:URIType=\"public\">" + CurrentVideo.VideoURI + "</res>" +
                                "</item>" +
                                "</DIDL-Lite>");

                    SetVideo.invoke(SetVideoCommands);
                    Thread.sleep(100);

                    Action Play = DeviceInfo.findAction("Play");
                    Map<String, String> PlayCommand = new HashMap<>();
                    PlayCommand.put("InstanceID", "0");
                    PlayCommand.put("Speed", "1");
                    Play.invoke(PlayCommand);

                    //Lets seek the video to the players time
                    if (CurrentVideo.CurrentTime != "00:00:00") {
                        Action Seek = DeviceInfo.findAction("Seek");
                        Map<String, String> SeekCommand = new HashMap<>();
                        SeekCommand.put("InstanceID", "0");
                        SeekCommand.put("Unit", "REL_TIME");
                        SeekCommand.put("Target", CurrentVideo.CurrentTime);
                        Seek.invoke(SeekCommand);
                    }


                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e){

                }
            }
        });

        thread.start();
        cp.stop();
        cp.terminate();
        finish();
    }




    public static void Seek(Device device, String Time){
        final Device DeviceInfo = device;
        final String CurrentTime = Time;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Lets seek the video to the players time
                    Action Seek = DeviceInfo.findAction("Seek");
                    Map<String, String> SeekCommand = new HashMap<>();
                    SeekCommand.put("InstanceID", "0");
                    SeekCommand.put("Unit", "REL_TIME");
                    SeekCommand.put("Target", CurrentTime);
                    Seek.invoke(SeekCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }



    void Stop(Device device){
        final Device DeviceInfo = device;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {

                    Device mediaServer = cp.getDevice(DeviceInfo.getUdn());           // get device by UDN

                    Action Stop = mediaServer.findAction("Stop"); // find "Browse" action
                    Map<String, String> arg = new HashMap<>();        // setup arguments
                    arg.put("InstanceID", "0");

                    try {
                        Stop.invoke(arg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        cp.stop();
        cp.terminate();
        finish();
    }
}
