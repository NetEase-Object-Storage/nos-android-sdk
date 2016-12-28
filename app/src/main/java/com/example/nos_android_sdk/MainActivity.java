package com.example.nos_android_sdk;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

import com.example.nos_android_sdk.R;
import com.netease.cloud.nos.android.core.AcceleratorConf;
import com.netease.cloud.nos.android.core.CallRet;
import com.netease.cloud.nos.android.core.Callback;
import com.netease.cloud.nos.android.core.WanNOSObject;
import com.netease.cloud.nos.android.core.UploadTaskExecutor;
import com.netease.cloud.nos.android.core.WanAccelerator;
import com.netease.cloud.nos.android.exception.InvalidChunkSizeException;
import com.netease.cloud.nos.android.exception.InvalidParameterException;
import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.utils.Util;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final String LOGTAG = LogUtil.makeLogTag(MainActivity.class);

	public static final int HTTP_UPLOAD = 0;
	public static final int HTTPS_UPLOAD = 1;
	public static final int NHTTP_UPLOAD = 2;
	public static final int NHTTPS_UPLOAD = 3;

	// 注意：这里的 AccessKey/SecretKey 仅为了方便测试，正式发布时请不要将其暴露给最终用户
	// 请向NOS值班popo：grp.nos@corp.netease.com
//	private final String BUCKET = "YOUR_BUCKET";
//	private final String accessKey = "YOUR_ACCESS_KEY";
//	private final String secretKey = "YOUR_SECRET_KEY";

	private final String BUCKET = "bbbb";
	private final String accessKey = "xxxxxx";
	private final String secretKey = "xxxxxx";

	private Button btnUpload;
	private Button btnHttps;

	private Button nbtnHttp;
	private Button nbtnHttps;
	private Button cancelBtn;

	private TextView hint;

	private EditText connectET;
	private EditText readET;
	private EditText fileET;
	private EditText retryET;
	private EditText refreshET;

	private UploadTaskExecutor executor;

	private WanNOSObject wanNOSObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		hint = (TextView) findViewById(R.id.txt);

		btnUpload = (Button) findViewById(R.id.btn);
		btnUpload.setOnClickListener(this);
		btnHttps = (Button) findViewById(R.id.btn1);
		btnHttps.setOnClickListener(this);

		nbtnHttp = (Button) findViewById(R.id.button1);
		nbtnHttp.setOnClickListener(this);
		nbtnHttps = (Button) findViewById(R.id.button2);
		nbtnHttps.setOnClickListener(this);
		cancelBtn = (Button) findViewById(R.id.button3);
		cancelBtn.setOnClickListener(this);

		connectET = (EditText) findViewById(R.id.connectET);
		readET = (EditText) findViewById(R.id.readET);
		fileET = (EditText) findViewById(R.id.fileET);
		retryET = (EditText) findViewById(R.id.retryET);
		refreshET = (EditText) findViewById(R.id.refreshET);

		wanNOSObject = new WanNOSObject();
		LogUtil.setLevel(0);

	}

	@Override
	public void onClick(View view) {

		hint.setText("");
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		String connectTimeout = connectET.getText().toString();
		String readTimeout = readET.getText().toString();
		String fileSize = fileET.getText().toString();
		String retry = retryET.getText().toString();
		String refresh = refreshET.getText().toString();

		// AcceleratorConf 可以不设置，默认配置会生效
		// Demo 会从 APP 输入获取相关配置
		AcceleratorConf conf = new AcceleratorConf();
		conf.setMonitorInterval(1000*60); // 监控超时设置为60s

		try {

			if (connectTimeout != null && !connectTimeout.equals(""))
				conf.setConnectionTimeout(Integer.parseInt(connectTimeout) * 1000);
			if (refresh != null && !refresh.equals(""))
				conf.setRefreshInterval(Integer.parseInt(refresh) * 1000);
			if (readTimeout != null && !readTimeout.equals(""))
				conf.setSoTimeout(Integer.parseInt(readTimeout) * 1000);
			if (fileSize != null && !fileSize.equals(""))
				conf.setChunkSize(Integer.parseInt(fileSize) * 1024);
			if (retry != null && !retry.equals("")) {
				conf.setChunkRetryCount(Integer.parseInt(retry));
				conf.setQueryRetryCount(Integer.parseInt(retry));
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (InvalidChunkSizeException e) {
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			e.printStackTrace();
			Toast.makeText(this,
					e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this,
					e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}


		WanAccelerator.setConf(conf);

		if (view.equals(btnUpload)) {
			LogUtil.d(LOGTAG, "upload by http");
			startActivityForResult(intent, HTTP_UPLOAD);
		} else if (view.equals(btnHttps)) {
			LogUtil.d(LOGTAG, "upload by https");
			startActivityForResult(intent, HTTPS_UPLOAD);
		} else if (view.equals(nbtnHttp)) {
			LogUtil.d(LOGTAG, "upload by new http");
			startActivityForResult(intent, NHTTP_UPLOAD);
		} else if (view.equals(nbtnHttps)) {
			LogUtil.d(LOGTAG, "upload by new https");
			startActivityForResult(intent, NHTTPS_UPLOAD);
		} else if (view.equals(cancelBtn)) {
			if (executor == null) {
				Toast.makeText(getApplicationContext(),
						"no uploading to cancel", Toast.LENGTH_SHORT).show();
			} else {
				LogUtil.d(LOGTAG, "cancel uploading.");
				executor.cancel();
				return;
			}
		}
	}

	/**
	 * 注：点击APP上的HTTP是同步上传
	 *    点击APP上的NHTTP是异步上传
	 */
	private void doUpload(Uri uri, final int code) {

		final File f = transformFile(uri);
		long expires = System.currentTimeMillis() / 1000 + 3600 * 24 * 30 * 12 * 10;

		try {
			final String token = Util.getToken(BUCKET, f.getName(), expires,
					accessKey, secretKey);
			LogUtil.d(LOGTAG, "token is: " + token);

			wanNOSObject.setNosBucketName(BUCKET);
			wanNOSObject.setUploadToken(token);
			wanNOSObject.setNosObjectName(f.getName());
			if (f.getName().contains(".jpg")) {
				wanNOSObject.setContentType("image/jpeg");
			}
			if (f.getName().contains(".mp4")) {
				wanNOSObject.setContentType("video/mp4");
			}

			if (code == NHTTP_UPLOAD) {
				String uploadContext = null;
				if (Util.getData(this, f.getAbsolutePath()) != null
						&& !Util.getData(this, f.getAbsolutePath()).equals("")) {
					uploadContext = Util.getData(this, f.getAbsolutePath());
				}

				executor = WanAccelerator.putFileByHttp(this, f,
						f.getAbsoluteFile(), uploadContext, wanNOSObject,
						new Callback() {
							@Override
							public void onUploadContextCreate(Object fileParam,
									String oldUploadContext,
									String newUploadContext) {
								LogUtil.e(LOGTAG, "context create: "
										+ fileParam + ", newUploadContext: "
										+ newUploadContext);
								Util.setData(MainActivity.this,
										fileParam.toString(), newUploadContext);
							}

							@Override
							public void onProcess(Object fileParam,
									long current, long total) {
								LogUtil.e(LOGTAG, "on process: " + current
										+ ", total: " + total);

								Toast.makeText(MainActivity.this,
										"onProcess " + current + "/" + total, Toast.LENGTH_SHORT)
										.show();
							}

							@Override
							public void onSuccess(CallRet ret) {
								LogUtil.e(
										LOGTAG,
										"on success: " + ret.getHttpCode()
												+ ", msg:" + ret.getResponse()
												+ ", context:"
												+ ret.getUploadContext()
												+ ", param:"
												+ ret.getFileParam()
												+ ", callbackMsg: "
												+ ret.getCallbackRetMsg());
								executor = null;
								Util.setData(MainActivity.this,
										f.getAbsolutePath(), "");
								Toast.makeText(MainActivity.this,
										"upload success", Toast.LENGTH_SHORT)
										.show();
							}

							@Override
							public void onFailure(CallRet ret) {
								LogUtil.d(
										LOGTAG,
										"on failure code: " + ret.getHttpCode()
												+ ", msg:" + ret.getResponse()
												+ ", context:"
												+ ret.getUploadContext()
												+ ", param:"
												+ ret.getFileParam());

								LogUtil.e(
										LOGTAG,
										"is cancel: "
												+ executor.isUpCancelled());
								executor = null;
								Toast.makeText(MainActivity.this,
										"upload fail", Toast.LENGTH_SHORT)
										.show();
							}

							@Override
							public void onCanceled(CallRet ret) {
								LogUtil.d(
										LOGTAG,
										"on calceled code: " + ret.getHttpCode()
												+ ", msg:" + ret.getResponse()
												+ ", context:"
												+ ret.getUploadContext()
												+ ", param:"
												+ ret.getFileParam());

								LogUtil.e(
										LOGTAG,
										"is cancel: "
												+ executor.isUpCancelled());
								executor = null;
								Toast.makeText(MainActivity.this,
										"upload cancel", Toast.LENGTH_SHORT)
										.show();
							}
						});
			}
			if (code == NHTTPS_UPLOAD) {
				String uploadContext = null;
				if (Util.getData(this, f.getAbsolutePath()) != null
						&& !Util.getData(this, f.getAbsolutePath()).equals("")) {
					uploadContext = Util.getData(this, f.getAbsolutePath());
				}
				executor = WanAccelerator.putFileByHttps(this, f,
						f.getAbsolutePath(), uploadContext, wanNOSObject,
						new Callback() {
							@Override
							public void onUploadContextCreate(Object fileParam,
									String oldUploadContext,
									String newUploadContext) {
								LogUtil.e(LOGTAG, "context create: "
										+ fileParam + ", newUploadContext: "
										+ newUploadContext);
								Util.setData(MainActivity.this,
										fileParam.toString(), newUploadContext);
							}

							@Override
							public void onProcess(Object fileParam,
									long current, long total) {
								LogUtil.e(LOGTAG, "on process: " + current
										+ ", total: " + total);

								Toast.makeText(MainActivity.this,
										"onProcess " + current + "/" + total,
										Toast.LENGTH_SHORT).show();
								
							}

							@Override
							public void onSuccess(CallRet ret) {
								LogUtil.e(
										LOGTAG,
										"on success: " + ret.getHttpCode()
												+ ", msg:" + ret.getResponse()
												+ ", context:"
												+ ret.getUploadContext()
												+ ", param:"
												+ ret.getFileParam());
								executor = null;
								Util.setData(MainActivity.this,
										f.getAbsolutePath(), "");
								Toast.makeText(MainActivity.this,
										"upload success", Toast.LENGTH_SHORT)
										.show();
							}

							@Override
							public void onFailure(CallRet ret) {
								LogUtil.d(
										LOGTAG,
										"on failure code: " + ret.getHttpCode()
												+ ", msg:" + ret.getResponse()
												+ ", context:"
												+ ret.getUploadContext()
												+ ", param:"
												+ ret.getFileParam());
								executor = null;
								Toast.makeText(MainActivity.this,
										"upload fail", Toast.LENGTH_SHORT)
										.show();
							}

							@Override
							public void onCanceled(CallRet ret) {
								LogUtil.d(
										LOGTAG,
										"on calceled code: " + ret.getHttpCode()
												+ ", msg:" + ret.getResponse()
												+ ", context:"
												+ ret.getUploadContext()
												+ ", param:"
												+ ret.getFileParam());

								LogUtil.e(
										LOGTAG,
										"is cancel: "
												+ executor.isUpCancelled());
								executor = null;
								Toast.makeText(MainActivity.this,
										"upload cancel", Toast.LENGTH_SHORT)
										.show();
							}
						});
			}

			if (code == HTTP_UPLOAD || code == HTTPS_UPLOAD) {
				new Thread(new Runnable() {
					@Override
					public void run() {
//						Message message = new Message();
						if (code == HTTP_UPLOAD) {
							String uploadContext = null;
							if (Util.getData(MainActivity.this,
									f.getAbsolutePath()) != null
									&& !Util.getData(MainActivity.this,
											f.getAbsolutePath()).equals("")) {
								uploadContext = Util.getData(MainActivity.this,
										f.getAbsolutePath());
							}

							try {

								executor = WanAccelerator.putFileByHttp(
										MainActivity.this, f,
										f.getAbsoluteFile(), uploadContext,
										wanNOSObject, new Callback() {
											@Override
											public void onUploadContextCreate(
													Object fileParam,
													String oldUploadContext,
													String newUploadContext) {
												LogUtil.e(
														LOGTAG,
														"context create: "
																+ fileParam
																+ ", newUploadContext: "
																+ newUploadContext);
												Util.setData(MainActivity.this,
														fileParam.toString(),
														newUploadContext);
											}

											@Override
											public void onProcess(
													Object fileParam,
													long current, long total) {
												LogUtil.e(LOGTAG, "on process: " + current
																+ ", total: " + total);

												Toast.makeText(MainActivity.this,
														"onProcess " + current + "/" + total,
														Toast.LENGTH_SHORT).show();
											}

											@Override
											public void onSuccess(CallRet ret) {
												LogUtil.e(
														LOGTAG,
														"on success: "
																+ ret.getHttpCode()
																+ ", msg:"
																+ ret.getResponse()
																+ ", context:"
																+ ret.getUploadContext()
																+ ", param:"
																+ ret.getFileParam());
												executor = null;
												Util.setData(MainActivity.this,
														f.getAbsolutePath(), "");
												Toast.makeText(
														MainActivity.this,
														"upload success",
														Toast.LENGTH_SHORT)
														.show();
											}

											@Override
											public void onFailure(CallRet ret) {
												LogUtil.d(
														LOGTAG,
														"on failure code: "
																+ ret.getHttpCode()
																+ ", msg:"
																+ ret.getResponse()
																+ ", context:"
																+ ret.getUploadContext()
																+ ", param:"
																+ ret.getFileParam());

												LogUtil.e(
														LOGTAG,
														"is cancel: "
																+ executor
																		.isUpCancelled());
												executor = null;
												Toast.makeText(
														MainActivity.this,
														"upload fail",
														Toast.LENGTH_SHORT)
														.show();
											}

											@Override
											public void onCanceled(CallRet ret) {
												LogUtil.d(
														LOGTAG,
														"on calceled code: " + ret.getHttpCode()
																+ ", msg:" + ret.getResponse()
																+ ", context:"
																+ ret.getUploadContext()
																+ ", param:"
																+ ret.getFileParam());

												LogUtil.e(
														LOGTAG,
														"is cancel: "
																+ executor.isUpCancelled());
												executor = null;
												Toast.makeText(MainActivity.this,
														"upload cancel", Toast.LENGTH_SHORT)
														.show();
											}
										});
								CallRet httpRet = executor.get();
								LogUtil.e(
										LOGTAG,
										"http get code: "
												+ httpRet.getHttpCode()
												+ ", https get method resut: "
												+ httpRet.getResponse());
							} catch (Exception ex) {
								LogUtil.e(LOGTAG, "http get method error: "
										+ ex.getMessage());
							}

						} else {
							String uploadContext = null;
							if (Util.getData(MainActivity.this,
									f.getAbsolutePath()) != null
									&& !Util.getData(MainActivity.this,
											f.getAbsolutePath()).equals("")) {
								uploadContext = Util.getData(MainActivity.this,
										f.getAbsolutePath());
							}
							try {
								executor = WanAccelerator.putFileByHttps(
										MainActivity.this, f,
										f.getAbsolutePath(), uploadContext,
										wanNOSObject, new Callback() {
											@Override
											public void onUploadContextCreate(
													Object fileParam,
													String oldUploadContext,
													String newUploadContext) {
												LogUtil.e(
														LOGTAG,
														"context create: "
																+ fileParam
																+ ", newUploadContext: "
																+ newUploadContext);
												Util.setData(MainActivity.this,
														fileParam.toString(),
														newUploadContext);
											}

											@Override
											public void onProcess(
													Object fileParam,
													long current, long total) {
												LogUtil.e(LOGTAG, "on process: " + current
																+ ", total: " + total);

												Toast.makeText(MainActivity.this,
														"onProcess " + current + "/" + total,
														Toast.LENGTH_SHORT).show();
											}

											@Override
											public void onSuccess(CallRet ret) {
												LogUtil.e(
														LOGTAG,
														"on success: "
																+ ret.getHttpCode()
																+ ", msg:"
																+ ret.getResponse()
																+ ", context:"
																+ ret.getUploadContext()
																+ ", param:"
																+ ret.getFileParam());
												executor = null;
												Util.setData(MainActivity.this,
														f.getAbsolutePath(), "");
												Toast.makeText(
														MainActivity.this,
														"upload success",
														Toast.LENGTH_SHORT)
														.show();
											}

											@Override
											public void onFailure(CallRet ret) {
												LogUtil.d(
														LOGTAG,
														"on failure code: "
																+ ret.getHttpCode()
																+ ", msg:"
																+ ret.getResponse()
																+ ", context:"
																+ ret.getUploadContext()
																+ ", param:"
																+ ret.getFileParam());
												executor = null;
												Toast.makeText(
														MainActivity.this,
														"upload fail",
														Toast.LENGTH_SHORT)
														.show();
											}

											@Override
											public void onCanceled(CallRet ret) {
												LogUtil.d(
														LOGTAG,
														"on calceled code: " + ret.getHttpCode()
																+ ", msg:" + ret.getResponse()
																+ ", context:"
																+ ret.getUploadContext()
																+ ", param:"
																+ ret.getFileParam());

												LogUtil.e(
														LOGTAG,
														"is cancel: "
																+ executor.isUpCancelled());
												executor = null;
												Toast.makeText(MainActivity.this,
														"upload cancel", Toast.LENGTH_SHORT)
														.show();
											}
										});
								CallRet httpsRet = executor.get();
								LogUtil.e(
										LOGTAG,
										"https get code: "
												+ httpsRet.getHttpCode()
												+ ", https get method resut: "
												+ httpsRet.getResponse());
							} catch (Exception ex) {
								LogUtil.e(LOGTAG, "https get method error: "
										+ ex.getMessage());
							}
						}
					}
				}).start();
			}

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private File transformFile(Uri uri) {
		String path = FileUtil.getPath(MainActivity.this, uri);
		File file = new File(path);
		return file;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		doUpload(data.getData(), requestCode);
	}

}
