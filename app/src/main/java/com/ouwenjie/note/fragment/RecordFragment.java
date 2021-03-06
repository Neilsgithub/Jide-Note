package com.ouwenjie.note.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFloat;
import com.ouwenjie.note.NoteApplication;
import com.ouwenjie.note.R;
import com.ouwenjie.note.activity.EditNoteActivity;
import com.ouwenjie.note.activity.MainActivity;
import com.ouwenjie.note.adapter.RecordAdapter;
import com.ouwenjie.note.db.NoteDatabaseHelper;
import com.ouwenjie.note.model.BaseNote;
import com.ouwenjie.note.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import yalantis.com.sidemenu.interfaces.ScreenShotable;

/**
 * Create at 2015年2月26日17:52:09 By ouwenjie
 */
public class RecordFragment extends Fragment implements View.OnClickListener ,ScreenShotable, RecordAdapter.OnItemClickListener, RecordAdapter.OnItemLongClickListener {

    // 切换动画 所需的两个成员
    private View containerView;
    private Bitmap bitmap;

    private RecyclerView recyclerView;
    private RecordAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private Activity activity;

    private ButtonFloat fab;

    private List<BaseNote> recordNoteList = new ArrayList<>();

    private NoteDatabaseHelper dbHelper = new NoteDatabaseHelper();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MemoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordFragment newInstance() {
        RecordFragment fragment = new RecordFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.container);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("RecordFragment"); //统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("RecordFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        fab = (ButtonFloat) view.findViewById(R.id.new_edit_fab);
        fab.setOnClickListener(this);

        recyclerView = (RecyclerView) view.findViewById(R.id.record_recyclerview);
        initRecyclerView();

        LogUtils.e("onCreateView");
        return view;
    }

    public void initRecyclerView() {
//        mLayoutManager = new LinearLayoutManager(activity);        // 创建一个线性布局管理器
//        mRecyclerView.setHasFixedSize(true);        // 提高性能（假如你能确定当你的内容改变时，item布局的大小不会改变时，可使用该方法提高性能）
//        mRecyclerView.setLayoutManager(mLayoutManager);        // 设置布局管理器

        layoutManager = new GridLayoutManager(activity,2);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecordAdapter(activity,myDataset());
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new SlideInLeftAnimator());

        recyclerView.getItemAnimator().setAddDuration(300);
        recyclerView.getItemAnimator().setRemoveDuration(300);
        recyclerView.getItemAnimator().setMoveDuration(300);
        recyclerView.getItemAnimator().setChangeDuration(300);
    }

    private List<BaseNote> myDataset() {
        TreeSet<BaseNote> allNote = ((MainActivity) activity).getNoteSet();
        for (BaseNote anAllNote : allNote) {
            if ((anAllNote.getNoteType() == BaseNote.NOTE_TYPE_RECORD || anAllNote.getNoteType() == BaseNote.NOTE_TYPE_REMIND)
                    && (anAllNote.getNoteState()==BaseNote.NOTE_STATE_VALID)) {
                recordNoteList.add(anAllNote);
            }
        }
        return recordNoteList;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public RecordAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(RecordAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.new_edit_fab:
                Intent recordIntent = new Intent(activity, EditNoteActivity.class);
                recordIntent.putExtra(BaseNote.KEY_DB_ID,-1);
                recordIntent.putExtra(BaseNote.KEY_NOTE_TYPE,BaseNote.NOTE_TYPE_RECORD);
                activity.startActivityForResult(recordIntent, BaseNote.REQ_NEW_NOTE);
                break;
            default:
                break;
        }
    }

    /**
     * 菜单栏转换栏目时的动画效果所需的方法之一
     */
    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                containerView.draw(canvas);
                RecordFragment.this.bitmap = bitmap;
            }
        };
        thread.start();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    // 当列表中的Item 被点击事，返回一个noteList 的 数据库的id
    @Override
    public void onItemClick(String dbID ) {
        BaseNote note = dbHelper.get(Long.valueOf(dbID));

        if(note != null) {
            Intent editIntent = new Intent(activity, EditNoteActivity.class);
            editIntent.putExtra(BaseNote.KEY_DB_ID, note.getId());

            adapter.removeItem(note);
            activity.startActivityForResult(editIntent, BaseNote.REQ_NEW_NOTE);
        }
    }

    @Override
    public void onItemLongClick(final String dbID) {

        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title("归档或删除？")
                .positiveText("归档")
                .negativeText("删除")
                .neutralText("取消")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        BaseNote note = dbHelper.get(Long.valueOf(dbID));
                        note.setNoteState(BaseNote.NOTE_STATE_INVALID); // 归档，NOTE状态为 INVALID,进入归档，并从当前列表中除去
                        note.save();
                        adapter.removeItem(note);
                        // Note 从 列表和数据库中删除后，需要更新 Application 和 Activity 中保存的 NoteSet ，避免再次显示。
                        refreshData();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        BaseNote note = dbHelper.get(Long.valueOf(dbID));
                        note.setNoteState(BaseNote.NOTE_STATE_DELETE);// 删除，NOTE 状态为 DELETE，进入回收站
                        note.save();
                        adapter.removeItem(note);
                        // Note 从 列表和数据库中删除后，需要更新 Application 和 Activity 中保存的 NoteSet ，避免再次显示。
                        refreshData();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog){
                    }
                }).build();

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void refreshData() {
        NoteApplication application = (NoteApplication) (activity.getApplication());
        application.updateNoteList();
        ((MainActivity) activity).setNoteSet(application.getNoteSet());
    }

}
