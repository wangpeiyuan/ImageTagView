package com.wpy.imagetagviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.wpy.imagetagview.SetTagGroupView;
import com.wpy.imagetagview.TagClickListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SetTagGroupView tagGroupView;
    private List<TagInfo> mList;
    private int mAddCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tagGroupView = (SetTagGroupView) findViewById(R.id.tagGroupView);

        tagGroupView.setTagClickListener(new TagClickListener() {
            @Override
            public void onClick(float x, float y) {
                if (mAddCount < mList.size()) {
                    tagGroupView.addTag(x, y, mList.get(mAddCount).tagList);
                    mAddCount++;
                }
            }

            @Override
            public void onTagEditClick(int index, View view) {
                Toast.makeText(MainActivity.this,
                        "onTagEditClick " + mList.get(index).toString(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongTagClick(int index, View view) {
                Toast.makeText(MainActivity.this,
                        "onLongTagClick  " + mList.get(index).toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        addTest();
    }

    private void addTest() {
        mList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        list.add("单条长标签 --- This is a long long long long tag");
        mList.add(new TagInfo(list));

        List<String> list1 = new ArrayList<>();
        list1.add("多条标签1");
        list1.add("more tag 2");
        list1.add("多条标签3");
        mList.add(new TagInfo(list1));
    }

    public class TagInfo {
        public List<String> tagList;

        private TagInfo(List<String> tagList) {
            this.tagList = tagList;
        }

        @Override
        public String toString() {
            return tagList.get(0);
        }
    }
}
