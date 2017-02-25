package com.hankyjcheng.imagecropper.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hankyjcheng.imagecropper.MainActivity;
import com.hankyjcheng.imagecropper.R;
import com.hankyjcheng.imagecropper.databinding.FragmentAccountEditBinding;

import java.io.File;
import java.io.IOException;

/**
 * Created by hankcheng on 1/14/2017.
 */

public class AccountEditFragment extends Fragment{

    private final int REQUEST_CAMERA = 100;
    private final int REQUEST_GALLERY = 200;
    public static final int REQUEST_IMAGE_CROP = 300;
    public static final String EXTRA_BITMAP = "EXTRA_BITMAP";

    private FragmentAccountEditBinding binding;
    private String tempCameraPhotoPath;
    private Bitmap bitmap;

    public static AccountEditFragment newInstance() {
        return new AccountEditFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("Fragment", "onCreateView");
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.fragment_account_edit, container, false);
        if (bitmap != null) {
            updateProfilePicture(bitmap);
        }
        else {
            useDefaultForImage();
        }
        binding.editImageButton.setOnClickListener(editImageButtonListener);
        binding.nameEditText.setSingleLine();
        //binding.nameEditText.setText(getUser().getName());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setToolbarTitle(getString(R.string.account_edit));
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_light_24dp);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (tempCameraPhotoPath != null) {
                        startImageCropFromCamera(tempCameraPhotoPath);
                    }
                    break;
                case REQUEST_GALLERY:
                    startImageCropFromGallery(data.getData());
                    break;
                case REQUEST_IMAGE_CROP:
                    Log.i("Crop", "Back");
                    if (data.hasExtra(EXTRA_BITMAP)) {
                        bitmap = data.getParcelableExtra(EXTRA_BITMAP);
                        binding.userImageView.setImageBitmap(bitmap);
                    }
                    break;
            }
        }
    }

    private View.OnClickListener editImageButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectImage();
        }
    };

    private void selectImage() {
        final CharSequence[] selectionList = {getString(R.string.profile_image_default),
                getString(R.string.profile_image_select_camera),
                getString(R.string.profile_image_select_gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.change_profile_image))
                .setItems(selectionList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (selectionList[i].equals(getString(R.string.profile_image_default))) {
                            useDefaultForImage();
                        }
                        else if (selectionList[i].equals(getString(R.string.profile_image_select_camera))) {
                            useCameraForImage();
                        }
                        else if (selectionList[i].equals(getString(R.string.profile_image_select_gallery))) {
                            userGalleryForImage();
                        }
                    }
                });
        builder.create().show();
    }

    private void useDefaultForImage() {
        Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_account_circle_grey_24dp);
        binding.userImageView.setImageBitmap(defaultIcon);
    }

    private void useCameraForImage() {
        PackageManager manager = getActivity().getPackageManager();
        if (manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(getContext(), getString(R.string.file_provider_authority), photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(cameraIntent, REQUEST_CAMERA);
                }
            }
            else{
                Toast.makeText(getActivity(), R.string.camera_not_available, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void userGalleryForImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_GALLERY);
    }

    private File createImageFile() throws IOException {
        String imageFileName = "profile_image_capture";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");
        tempCameraPhotoPath = image.getAbsolutePath();
        Log.i("TempPhotoPath", tempCameraPhotoPath + "");
        return image;
    }

    private void updateProfilePicture(Bitmap bitmap) {
        Log.i("Bitmap Size", bitmap.getAllocationByteCount() + "");
        binding.userImageView.setImageBitmap(bitmap);
    }

    public void startImageCropFromCamera(@NonNull String filePath) {
        AccountImageCropFragment fragment = AccountImageCropFragment.newInstance(filePath);
        fragment.setTargetFragment(this, REQUEST_IMAGE_CROP);
        getFragmentManager().beginTransaction()
                .replace(((MainActivity) getActivity()).getContainerId(), fragment)
                .addToBackStack(this.toString())
                .commit();
    }

    public void startImageCropFromGallery(@NonNull Uri uri) {
        AccountImageCropFragment fragment = AccountImageCropFragment.newInstance(uri);
        fragment.setTargetFragment(this, REQUEST_IMAGE_CROP);
        getFragmentManager().beginTransaction()
                .replace(((MainActivity) getActivity()).getContainerId(), fragment)
                .addToBackStack(this.toString())
                .commit();
    }

}