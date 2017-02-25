package com.hankyjcheng.imagecropper.fragment;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.hankyjcheng.imagecropper.MainActivity;
import com.hankyjcheng.imagecropper.R;
import com.hankyjcheng.imagecropper.databinding.FragmentAccountImageCropBinding;

import java.io.File;
import java.io.IOException;

/**
 * Created by hankcheng on 1/14/2017.
 */

public class AccountImageCropFragment extends Fragment {

    private FragmentAccountImageCropBinding binding;
    private Bitmap bitmap;
    private static final String BITMAP_CAMERA = "bitmapCamera";
    private static final String BITMAP_GALLERY = "bitmapGallery";
    private final int image_max_size = 200;

    public static AccountImageCropFragment newInstance(String filePath) {
        AccountImageCropFragment fragment = new AccountImageCropFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BITMAP_CAMERA, filePath);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static AccountImageCropFragment newInstance(Uri uri) {
        AccountImageCropFragment fragment = new AccountImageCropFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BITMAP_GALLERY, uri.toString());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(BITMAP_GALLERY)) {
                createBitmapFromGallery(bundle);
            }
            else if (bundle.containsKey(BITMAP_CAMERA)) {
                createBitmapFromCamera(bundle);
            }
            if (bitmap == null) {
                cancelCrop();
            }
        }
        else {
            cancelCrop();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account_image_crop, container, false);
        binding.setHandler(this);
        binding.confirmButton.setOnClickListener(confirmButtonListener);
        ViewTreeObserver observer = binding.photoImageView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.photoImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initBitmap();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setToolbarTitle(getString(R.string.profile_image_crop));
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_light_24dp);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, 10, 10, false);
        }
    }

    private View.OnClickListener confirmButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            confirmCrop();
        }
    };

    private void createBitmapFromGallery(Bundle bundle) {
        Uri imageUri = Uri.parse(bundle.getString(BITMAP_GALLERY));
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createBitmapFromCamera(Bundle bundle) {
        String filePath = bundle.getString(BITMAP_CAMERA);
        if (filePath != null) {
            File imageFile = new File(filePath);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
        }
    }

    private void initBitmap() {
        float ratio = (float) bitmap.getWidth() / bitmap.getHeight();
        int imageViewWidth;
        int imageViewHeight;
        if (bitmap.getWidth() < image_max_size || bitmap.getHeight() < image_max_size) {
            if (bitmap.getWidth() < bitmap.getHeight()) {
                bitmap = Bitmap.createScaledBitmap(bitmap, image_max_size,
                        (int) (image_max_size / ratio), false);
                if (bitmap.getHeight() > binding.imageCropLayout.getHeight()) {
                    bitmap = Bitmap.createBitmap(bitmap,
                            0,
                            (bitmap.getHeight() - binding.imageCropLayout.getHeight()) / 2,
                            bitmap.getWidth(),
                            binding.imageCropLayout.getHeight());
                }
            }
            else {
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        (int) (image_max_size * ratio), image_max_size, false);
                if (bitmap.getWidth() > binding.imageCropLayout.getWidth()) {
                    bitmap = Bitmap.createBitmap(bitmap,
                            (bitmap.getWidth() - binding.imageCropLayout.getWidth()) / 2,
                            0,
                            binding.imageCropLayout.getWidth(),
                            bitmap.getHeight());
                }
            }
            imageViewWidth = bitmap.getWidth();
            imageViewHeight = bitmap.getHeight();
        }
        else {
            // Bitmap horizontal
            if (bitmap.getWidth() > bitmap.getHeight() &&
                    bitmap.getWidth() > binding.imageCropLayout.getWidth()) {
                imageViewWidth = binding.imageCropLayout.getWidth();
                imageViewHeight = (int) (binding.imageCropLayout.getWidth() / ratio);
                bitmap = Bitmap.createScaledBitmap(bitmap, imageViewWidth, imageViewHeight, false);
            }
            // Bitmap vertical
            else if (bitmap.getWidth() < bitmap.getHeight() &&
                    bitmap.getHeight() > binding.imageCropLayout.getHeight()) {
                imageViewWidth = (int) (binding.imageCropLayout.getHeight() * ratio);
                imageViewHeight = binding.imageCropLayout.getHeight();
                bitmap = Bitmap.createScaledBitmap(bitmap, imageViewWidth, imageViewHeight, false);
            }
            else {
                imageViewWidth = bitmap.getWidth();
                imageViewHeight = bitmap.getHeight();
            }
            binding.imageCropLayout.setRadiusMax(imageViewWidth > imageViewHeight ? imageViewHeight / 2 : imageViewWidth / 2);
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.photoImageView.getLayoutParams();
        layoutParams.width = imageViewWidth;
        layoutParams.height = imageViewHeight;
        binding.photoImageView.setLayoutParams(layoutParams);

        binding.imageCropLayout.setChildImageView(binding.photoImageView);
        binding.photoImageView.setImageBitmap(bitmap);
    }

    private void confirmCrop() {
        bitmap = binding.imageCropLayout.getCroppedBitmap(bitmap);
        if (bitmap.getWidth() > image_max_size && bitmap.getHeight() > image_max_size) {
            bitmap = Bitmap.createScaledBitmap(bitmap, image_max_size, image_max_size, false);
        }
        Intent intent = getActivity().getIntent();
        intent.putExtra(AccountEditFragment.EXTRA_BITMAP, bitmap);
        getTargetFragment().onActivityResult(
                AccountEditFragment.REQUEST_IMAGE_CROP,
                Activity.RESULT_OK,
                intent);
        getActivity().onBackPressed();
    }

    private void cancelCrop() {
        getTargetFragment().onActivityResult(
                AccountEditFragment.REQUEST_IMAGE_CROP,
                Activity.RESULT_OK,
                getActivity().getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}