package com.hankyjcheng.imagecropper;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.hankyjcheng.imagecropper.databinding.ActivityMainBinding;
import com.hankyjcheng.imagecropper.fragment.AccountEditFragment;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private AccountEditFragment accountEditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setHandler(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        accountEditFragment = AccountEditFragment.newInstance();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, accountEditFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, accountEditFragment)
                    .commit();
        }
        else {
            super.onBackPressed();
        }
    }

    public void setToolbarTitle(String title) {
        binding.toolbarTitleTextView.setText(title);
    }

    public int getContainerId() {
        return R.id.container;
    }

}
