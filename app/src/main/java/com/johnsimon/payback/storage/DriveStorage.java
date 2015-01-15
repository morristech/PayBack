package com.johnsimon.payback.storage;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.internal.p;
import com.johnsimon.payback.core.Callback;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.DataSyncer;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.williammora.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DriveStorage extends Storage implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final static int REQUEST_CODE_RESOLUTION = 14795;
    private final static String FILE_NAME = "data.json";

    public Activity activity;

    private GoogleApiClient client;

    private LocalStorage localStorage;

    private DriveFile file = null;

    public DriveStorage(Activity context) {
        super(context);
		//TODO migrate between

        activity = context;

        localStorage = new LocalStorage(context);
        localStorage.subscription.listen(localStorageDataRecieved);

        client = new GoogleApiClient.Builder(context)
           .addApi(Drive.API)
		//TODO innan release: använda app folder
           .addScope(Drive.SCOPE_FILE)
           .addConnectionCallbacks(this)
           .addOnConnectionFailedListener(this)
           .build();

	}

    private Callback<AppData> localStorageDataRecieved = new Callback<AppData>() {
        @Override
        public void onCalled(AppData data) {
            show("emit localStorage data");
            emit(data);
        }
    };

    public void sync(AppData driveData) {
		show("checking for changes from drive");

		if(!driveData.equals(localStorage.data)) {
			show("found changes and synced");

			AppData data = DataSyncer.sync(driveData, localStorage.data);

			commit(data);
			emit(data);
		}
    }

    @Override
    public void commit() {
        show("commited data to localStorage");
        localStorage.commit(data);


        if(file == null) return;
        write(data.save(), file, new ResultCallback<FileResult>() {
            @Override
            public void onResult(FileResult result) {
                if(!result.getStatus().isSuccess()) {
                    error("Error when commiting", result.getStatus());
                    return;
                }
                show("commited data to drive");
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
		refresh();
    }

	private void refresh() {
		lastRefresh = System.currentTimeMillis();
		Drive.DriveApi.requestSync(client).setResultCallback(requestSyncCallback);
	}

	@Override
	public void requestRefresh() {
		if(client.isConnected() && mayRefresh()) {
			refresh();
		}
	}

	private final static long MAX_REFRESH_FREQ = 5000;
	private Long lastRefresh;
	private boolean mayRefresh() {
		return System.currentTimeMillis() - lastRefresh > MAX_REFRESH_FREQ;
	}

	public void logout() {
		client.clearDefaultAccountAndReconnect();
		emit(new AppData());
		localStorage.commit(data);
	}

	private ResultCallback<Status> requestSyncCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if(!status.isSuccess()) {
                show("Sync error");
            }

            Drive.DriveApi.getRootFolder(client)
				.listChildren(client)
				.setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
					@Override
					public void onResult(DriveApi.MetadataBufferResult result) {
						if (!result.getStatus().isSuccess()) {
							error("Error listing children", result.getStatus());
							return;
						}

						MetadataBuffer buffer = result.getMetadataBuffer();
						int count = buffer.getCount();
						if (count > 0) {
							Metadata data = buffer.get(0);
							//show("File exists " + count);

							read(data.getDriveId(), fileFoundCallback);
						} else {
							//show("File doesn't exists");

							createFile(data.save(), fileCreatedCallback);
						}
						buffer.release();
					}
				});
        }
    };

    private ResultCallback<FileResult> fileFoundCallback = new ResultCallback<FileResult>() {
        @Override
        public void onResult(FileResult result) {
            if(!result.getStatus().isSuccess()) {
                error("Error when reading file", result.getStatus());
                return;
            }

			setFile(result.getFile());

            String JSON = result.getText();

            //show("File says " + JSON);

            sync(AppData.fromJson(JSON));
        }
    };

    private ResultCallback<FileResult> fileCreatedCallback = new ResultCallback<FileResult>() {
        @Override
        public void onResult(FileResult result) {
            if (!result.getStatus().isSuccess()) {
                error("Error while trying to write file", result.getStatus());
                return;
            }

			setFile(result.getFile());

            show("Created a file");
        }
    };


	private void setFile(DriveFile file) {
		this.file = file;
	}

    private void createFile(final String text, final ResultCallback<FileResult> callback) {
        Drive.DriveApi.newDriveContents(client)
            .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        error("Error while trying to create new file contents", result.getStatus());
                        return;
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(FILE_NAME)
                        .setMimeType("text/json")
                        .build();


                    Drive.DriveApi.getRootFolder(client)
                        .createFile(client, changeSet, result.getDriveContents())
                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onResult(DriveFolder.DriveFileResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    error("Error while trying to create file", result.getStatus());
                                    return;
                                }

                                write(text, result.getDriveFile(), callback);
                            }
                        });
                }
            });
    }

    private void read(final DriveId id, ResultCallback<FileResult> callback) {
        read(Drive.DriveApi.getFile(client, id), callback);
    }

    private void write(final String text, final DriveFile file, final ResultCallback<FileResult> callback) {
        file.open(client, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
			@Override
			public void onResult(DriveApi.DriveContentsResult result) {
				if (!result.getStatus().isSuccess()) {
					callback.onResult(new FileResult(result.getStatus()));
					return;
				}

				DriveContents contents = result.getDriveContents();
				try {
					contents.getOutputStream().write(text.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				contents.commit(client, null).setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						callback.onResult(new FileResult(file, text, status));
					}
				});
			}
		});

    }

    private void read(final DriveFile file, final ResultCallback<FileResult> callback) {
        file.open(client, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    callback.onResult(new FileResult(result.getStatus()));
                    return;
                }

                DriveContents contents = result.getDriveContents();
                BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
                StringBuilder builder = new StringBuilder();
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callback.onResult(new FileResult(file, builder.toString(), result.getStatus()));
            }
        });
    }

	private boolean keepAlive = false;
    @Override
    public void connect() {
		if(!client.isConnected()) {
			client.connect();
		} else {
			requestRefresh();
			keepAlive = true;
		}
    }

    @Override
    public void disconnect() {
		if(!keepAlive) {
			client.disconnect();
		}
		keepAlive = false;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
                show("Shit fuckd up");
                //client.connect();
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
        }
    }

    @Override
    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == Activity.RESULT_OK) {
                    client.connect();
                }
                return true;
        }
        return super.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void show(String text) {
        Snackbar.with(activity.getApplicationContext())
                .text(text)
                .show(activity);
    }

	protected void error(String title, Status status) {
		new MaterialDialog.Builder(activity)
				.title(title)
				.content(status.getStatus() + ": " + status.getStatusMessage())
				.show();
	}

    private static class FileResult implements Result {
        private DriveFile file;
        private String text;
        private Status status;

        public FileResult(DriveFile file, String text, Status status) {
            this.file = file;
            this.text = text;
            this.status = status;
        }

        public FileResult(Status status) {
            this(null, null, status);
        }

        public DriveFile getFile() {
            return file;
        }

        public String getText() {
            return text;
        }

        @Override
        public Status getStatus() {
            return status;
        }
    }
}