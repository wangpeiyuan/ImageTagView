package com.wpy.imagetagviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.wpy.imagetagview.tag.ImageTagViewGroup;
import com.wpy.imagetagview.tag.TagGroupClickListener;
import com.wpy.imagetagview.tag.other.SetTagGroupView;
import com.wpy.imagetagview.tag.other.TagClickListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SetTagGroupView tagGroupView;
    private List<TagInfo> mList;
    private int mAddCount = 0;

    private ImageTagViewGroup imageTagViewGroup;

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

        imageTagViewGroup = (ImageTagViewGroup) findViewById(R.id.imageTagViewGroup);
        imageTagViewGroup.setOnTagGroupClickListener(new TagGroupClickListener() {
            @Override
            public void onClick(float x, float y) {
                addTest(x, y);
            }

            @Override
            public void onTypeChange(int index, int type) {
                Log.d(TAG, "onTypeChange: index = " + index + " type = " + type);
            }

            @Override
            public void onTagLongClick(int index) {
                Toast.makeText(MainActivity.this, "delete " + index, Toast.LENGTH_SHORT).show();
                imageTagViewGroup.removeTagChild(index);
            }
        });
    }


    private void addTest(float x, float y) {
        TestTagContent testTagContent = new TestTagContent();
        testTagContent.addTest(x, y);
        imageTagViewGroup.addTag(testTagContent);
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
