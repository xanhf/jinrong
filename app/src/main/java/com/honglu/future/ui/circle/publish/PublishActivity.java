package com.honglu.future.ui.circle.publish;

import android.content.Intent;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.base.BaseActivity;
import com.honglu.future.util.AndroidUtil;
import com.honglu.future.util.DeviceUtils;
import com.honglu.future.util.ToastUtil;
import com.honglu.future.widget.photopicker.ImagesSelectorActivity;
import com.honglu.future.widget.photopicker.PhotoPreviewActivity;
import com.honglu.future.widget.photopicker.PublishImgAdapter;
import com.honglu.future.widget.photopicker.SelectorSettings;
import com.honglu.future.widget.uploader.UploadProgressListener;
import com.honglu.future.widget.uploader.Uploader;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class PublishActivity extends BaseActivity {
    private static final String TAG = "PublishActivity";
    public final static int REQUEST_CODE = 1;
    public final static int PHOTO_PREVIEW_CODE = 2;
    @BindView(R.id.tv_title)
    TextView mTitleText;
    @BindView(R.id.tv_right)
    TextView mTvRight;
    private EditText mContentEdit;
    private PublishImgAdapter mAdapter;
    private ArrayList<String> mSelectedPhotos = new ArrayList<>();
    private ArrayList<String> mUrls;


    @Override
    public int getLayoutId() {
        return R.layout.activity_publish;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void loadData() {
        initViews();
    }

    private void initViews() {
        mTitle.setTitle(false, R.color.white, "哞一下");
        mTvRight.setText("发表");
        mTvRight.setTextColor(getResources().getColor(R.color.white));
        mTvRight.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_blue_2dp_bg));
        mTvRight.setGravity(Gravity.CENTER_VERTICAL);
        GridView publishGirdView = (GridView) findViewById(R.id.publish_gridView);
        mContentEdit = (EditText) findViewById(R.id.editText_content);
        AndroidUtil.setEmojiFilter(mContentEdit);
        mAdapter = new PublishImgAdapter();
        mUrls = new ArrayList<>();
        mAdapter.setOnPublishClickCallBack(createOnPublishClickCallBack());
        mAdapter.refreshPhotos(mSelectedPhotos);
        publishGirdView.setAdapter(mAdapter);

        mTvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DeviceUtils.isFastDoubleClick()) {
                    return;
                }
                publish();
            }
        });
    }


    private PublishImgAdapter.PublishClickCallBack createOnPublishClickCallBack() {
        return new PublishImgAdapter.PublishClickCallBack() {
            @Override
            public void toAddImage() {
                Intent intent = new Intent(PublishActivity.this, ImagesSelectorActivity.class);
                intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 6);
                intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000);
                intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true);
                intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, mSelectedPhotos);
                startActivityForResult(intent, REQUEST_CODE);
            }

            @Override
            public void onClick(int position) {
                Intent intent = new Intent(PublishActivity.this, PhotoPreviewActivity.class);
                intent.putExtra(ImagesSelectorActivity.EXTRA_CURRENT_ITEM, position);
                intent.putExtra(ImagesSelectorActivity.EXTRA_PHOTOS, mSelectedPhotos);
                intent.putExtra(ImagesSelectorActivity.EXTRA_SHOW_DELETE, true);
                startActivityForResult(intent, PHOTO_PREVIEW_CODE);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            mSelectedPhotos.clear();
            if (data != null) {
                mSelectedPhotos = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);
            }
            assert mSelectedPhotos != null;
            mAdapter.refreshPhotos(mSelectedPhotos);
        } else if (resultCode == RESULT_OK && requestCode == PHOTO_PREVIEW_CODE) {
            mSelectedPhotos.clear();
            if (data != null) {
                mSelectedPhotos = data.getStringArrayListExtra(ImagesSelectorActivity.KEY_SELECTED_PHOTOS);
            }
            assert mSelectedPhotos != null;
            mAdapter.refreshPhotos(mSelectedPhotos);
        }
    }

    private void publish() {
        if (mContentEdit.getText().toString().trim().length() <= 0) {
            ToastUtil.show("说点什么吧");
            return;
        }
        mUrls.clear();
        if (mSelectedPhotos.size() == 0){
            publishArticles();
        }else{
            upload();
        }
    }
    private Handler mHandler = new Handler();
    private void upload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(mSelectedPhotos.get(mUrls.size()));
                float size = (float) file.length() / (float) 1024;
                if (size < 200f) {
                    uploadToServer(file);
                } else {
                    Luban.get(PublishActivity.this)
                            .load(new File(mSelectedPhotos.get(mUrls.size()))) //传人要压缩的图片
                            .putGear(Luban.THIRD_GEAR)      //设定压缩档次，默认三挡
                            .setCompressListener(new OnCompressListener() { //设置回调
                                @Override
                                public void onStart() {
                                }

                                @Override
                                public void onSuccess(final File file) {
                                    uploadToServer(file);
                                }

                                @Override
                                public void onError(Throwable e) {
                                }
                            }).launch();    //启动压缩
                }
            }
        }).start();
    }

    /**
     * 上传图片到服务器
     */
    private void uploadToServer(final File file) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Uploader.upload("http://192.168.90.139:8080/futures-community-api/app/circle/saveImage", file, new UploadProgressListener() {
                    @Override
                    public void onProgress(long progress) {
                    }
                    @Override
                    public void onSucceed(byte[] response) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response));
                            String url = jsonObject.getString("data");
                            mUrls.add(url);
                            if (mUrls.size() == mSelectedPhotos.size()) {
                                publishArticles();
                            } else {
                                upload();
                            }
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onError() {
                        ToastUtil.show("上传图片大小过大！");
                    }
                });
            }
        });
    }

    /**
     * 发表
     */
    private void publishArticles() {

    }


}