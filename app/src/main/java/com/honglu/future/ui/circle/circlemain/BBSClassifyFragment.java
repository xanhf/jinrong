package com.honglu.future.ui.circle.circlemain;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.ui.circle.circlemain.adapter.BBSAdapter;
import com.honglu.future.util.ImageUtil;
import com.honglu.future.widget.ExpandableLayout;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;

public class BBSClassifyFragment extends PagerFragment {

    public static final String EXTRA_CLASSIFY_TYPE = "filter_type";
    private View empty_view;
    private View mNewMsgLy;
    private TextView mNewMessageTV;
    private ImageView mNewMsgFromPortraitIV;
    private ListView mListView;
    private SmartRefreshLayout srl_refreshView;
    private TextView mNewNumTv;
    private ExpandableLayout mExpandableLy;

    private BBSAdapter mAdapter;

    private boolean isLoadingNow = false;
    //private boolean isLoadingFinished = false;

    private String topicType = "1", topic_id = "0";
    private String topic_id_temp = "0", fid = "0";
    private int messageTabPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        //监听关注
//        MessageController.getInstance().setDetailFriendChange(new MessageController.DetailFriendChange() {
//            @Override
//            public void change(String uid, String flower) {
//                List<BBS> list = mAdapter.getList();
//                if (list != null) {
//                    for (BBS bbs : list) {
//                        if (TextUtils.equals(bbs.uid,uid)) {
//                            bbs.follow = flower;
//                        }
//                    }
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//        });
        //监听点赞
//        MessageController.getInstance().setZanChange(new MessageController.ZanChange() {
//            @Override
//            public void zanchange(BBS bbs) {
//                List<BBS> list = mAdapter.getList();
//                if (list != null) {
//                    for (BBS b : list) {
//                        if (b.topic_id.equals(bbs.topic_id)) {
//                            b.attutude = "1";
//                            AttutudeUser a = new AttutudeUser();
//                            a.headimgurl = SPUtil.getString(getContext(), "headimg", "");
//                            a.uid = SPUtil.getString(getContext(), "user_id", "");
//                            a.user_name = SPUtil.getString(getContext(), "user_name", "");
//                            b.attutude_user.add(a);
//                            b.support_num = bbs.support_num;
//                            mAdapter.notifyDataSetChanged();
//                        }
//                    }
//                }
//            }
//        });
    }

    @Override
    protected int doGetContentLayout() {
        return R.layout.fragment_bbs_newest;
    }

//    public void onEventMainThread(TopicInfoEntity entity) {
//        if(mAdapter!=null){
//            List<BBS> list = mAdapter.getList();
//            if (list != null) {
//                for (BBS bbs : list) {
//                    if (TextUtils.equals(bbs.topic_id,entity.topicId)) {
//                        bbs.reply_num = entity.replyCount;
//                    }
//                }
//                mAdapter.notifyDataSetChanged();
//            }
//        }
//    }

    /**
     * 评论
     */
    public void onEventMainThread() {
//        if(mAdapter!=null){
//            List<BBS> list = mAdapter.getList();
//            if (list != null) {
//                for (BBS bbs : list) {
//                    if (TextUtils.equals(bbs.topic_id,entity.topicId)) {
//                        bbs.reply_num = entity.replyCount;
//                    }
//                }
//                mAdapter.notifyDataSetChanged();
//            }
//        }
    }


    @Override
    protected void doInit(View root) {
        topicType = getArguments().getString(EXTRA_CLASSIFY_TYPE);
        srl_refreshView = (SmartRefreshLayout) root.findViewById(R.id.srl_refreshView);
        mNewNumTv = (TextView) root.findViewById(R.id.tv_bbs_new);
        mListView = (ListView) root.findViewById(R.id.lv_listView);
        empty_view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_bbs_empty, null);
        TextView empty_text = (TextView) empty_view.findViewById(R.id.empty_tv);
//        if (!BBSFragmentNew.TOPIC_TYPE_ATTENTION.equals(topicType)) {
//            empty_text.setText("还没有最新的消息哦~");
//        }
        empty_text.setText("还没有最新的消息哦~");
        mAdapter = new BBSAdapter(mListView, getContext(), getLoadMoreListener());
        mAdapter.setTopicType(topicType);
        mAdapter.setToRefreshListViewListener(new BBSAdapter.ToRefreshListViewListener() {
            @Override
            public void refresh() {
                //isLoadingFinished = false;
              //  topicIndexThread("pull_down", topicType, false);
            }
        });
        mListView.setAdapter(mAdapter);
        mNewMsgLy = root.findViewById(R.id.message_new_ly);
        mNewMsgLy.setOnClickListener(getReaderMessageListener());
        mNewMessageTV = (TextView) root.findViewById(R.id.tv_new_message);
        mNewMsgFromPortraitIV = (ImageView) root.findViewById(R.id.iv_from_portrait);
        mExpandableLy = (ExpandableLayout) root.findViewById(R.id.exly_container);
    }

    @Override
    protected void doLazyLoadData() {
        super.doLazyLoadData();
        refreshData();
    }


    private void refreshData() {
        if (!isLoadingNow) {
            //isLoadingFinished = false;
            topic_id_temp = "0";
            topicIndexThread("pull_down", topicType, true);
        }
    }

//    public void onEventMainThread(BBSIndicatorEvent event) {
//        if (event.getTopicType().equals(topicType)) {
//            if (event.isBackTop()) {
//                mListView.setSelection(0);
//            }
//            refreshData();
//        }
//    }

    // 滑动加载更多
    public BBSAdapter.ScrollToLastCallBack getLoadMoreListener() {
        return new BBSAdapter.ScrollToLastCallBack() {
            @Override
            public void onScrollToLast(Integer pos) {
                if (!isLoadingNow) {
                    topicIndexThread("pull_up", topicType, false);
                }
            }
        };
    }

    private View.OnClickListener getReaderMessageListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewMsgLy.setVisibility(View.GONE);
//                Intent intent = new Intent(getActivity(), BBSMessageActivity.class);
//                intent.putExtra(BBSMessageActivity.EXTRA_MESSAGE_POSITION, messageTabPosition);
//                startActivity(intent);
                messageTabPosition = 0;
            }
        };
    }

    private void topicIndexThread(final String pull_style, String type, final boolean isNeedToShowNewEst) {
        if (isLoadingNow) {
            return;
        }
        if (mAdapter.getCount() == 0) {
            showLoadingPage(R.id.ly_loading_container);
        }
        isLoadingNow = true;

        //判断是下拉刷新还是加载更多
        final boolean isRefresh = TextUtils.equals("pull_down", pull_style);
        //根据情况设置当前的topic_id请求参数
      //  topic_id = isRefresh ? "0" : mAdapter.getItem(mAdapter.getCount() - 1).topic_id;

//        ServerAPI.topicIndex(getContext(), type, topic_id, fid, null, new ServerCallBack<TopicList>() {
//                    @Override
//                    public void onSucceed(Context context, TopicList result) {
//                        if (result != null && !TextUtils.isEmpty(result.mid)) {
//                            SPUtil.setString(context, "mid", result.mid);
//                        }
//                        mExpandableLy.collapse();
//                        if (result != null && !TextUtils.isEmpty(result.new_num)) {
//                            int newNum = Integer.valueOf(result.new_num);
//                            if (newNum > 0) {
//                                if (isNeedToShowNewEst) {
//                                    mNewNumTv.setText("已成功加载" + newNum + "条动态");
//                                    mExpandableLy.expand();
//                                    mExpandableLy.postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            mExpandableLy.collapse();
//                                        }
//                                    }, 1600);
//                                }
//                            }
//                        }
//                        performNewMessage(result.topicMsg, result.topicMsgImg);
//                        try {
//                            String topicMsgType = result.topicMsgType;
//                            if (!TextUtils.isEmpty(topicMsgType)) {
//                                int position = Integer.valueOf(result.topicMsgType);
//                                if (position > 0) {
//                                    messageTabPosition = position - 1;
//                                }
//                            } else {
//                                messageTabPosition = 0;
//                            }
//                        } catch (Exception e) {
//                            messageTabPosition = 0;
//                        }
//
//                        if (result.topicList != null && result.topicList.size() > 0) {
//                            if (mListView.getFooterViewsCount() != 0)
//                                mListView.removeFooterView(empty_view);
//
//                            if (isRefresh) {
//                                mAdapter.clearDatas();
//                                mAdapter.setDatas(result.topicList);
//                            } else {
//                                mAdapter.setDatas(result.topicList);
//                            }
//
//                        } else {
//                            //空布局
//                            if (mListView.getFooterViewsCount() == 0 && mAdapter.getCount() == 0) {
//                                mListView.addFooterView(empty_view, null, false);
//                            }
//                        }
//
//                        closeLoadingPage();
//                    }
//
//                    @Override
//                    public void onError(Context context, String errorMsg) {
//                        if (mAdapter.getCount() == 0) {
//                            setLoadFailedMessage(errorMsg);
//                        }
//                    }
//
//                    @Override
//                    public void onFinished(Context context) {
//                        isLoadingNow = false;
//                        mPullToRefreshListView.onRefreshComplete();
//                    }
//                }

//        );
    }

    private void performNewMessage(String newMessage, String fromPortrait) {
        if (!TextUtils.isEmpty(newMessage)) {
            mNewMsgLy.setVisibility(View.VISIBLE);
            mNewMessageTV.setText(newMessage);
            ImageUtil.display(fromPortrait, mNewMsgFromPortraitIV, R.mipmap.img_head);
        } else {
            mNewMsgLy.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onReload(Context context) {
        super.onReload(context);
        //isLoadingFinished = false;
        topic_id_temp = "0";
        topicIndexThread("pull_down", topicType, true);
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void loadData() {

    }
}
