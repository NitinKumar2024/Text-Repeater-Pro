package com.nitin.textrepeaterpro.ui.gallery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nitin.textrepeaterpro.R;
import com.nitin.textrepeaterpro.databinding.FragmentGalleryBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    ProgressDialog progressBar;

    private ImageView imageView;
    private EditText repetitionEditText;
    private Button importImageButton;
    private Button sendButton;

    private FragmentGalleryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        imageView = binding.imageView;
        CardView importImageButton = binding.importImageButton;
        repetitionEditText = binding.repetitionEditText;
        sendButton = binding.sendButton;

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    // Parse the input value and check if it's within the allowed range
                    int input = Integer.parseInt(dest.subSequence(0, dstart).toString() + source.subSequence(start, end).toString());
                    if (input <= 200) {
                        return null; // Accept the input
                    }
                } catch (NumberFormatException e) {
                    // Handle the case where the input is not a valid number
                }

                // Reject the input
                return "";
            }
        };

        repetitionEditText.setFilters(new InputFilter[]{filter});

        progressBar = new ProgressDialog(getActivity());
        progressBar.setMessage("Loading...");

        importImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity(), "Wait...", Toast.LENGTH_SHORT).show();

                repeatAndSendImages();
            }
        });



        return root;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void repeatAndSendImages() {

        // Get the repetition count from the EditText
        String repetitionCountString = repetitionEditText.getText().toString();

        if (repetitionCountString.isEmpty()) {
            Toast.makeText(getActivity(), "Enter a repetition count", Toast.LENGTH_SHORT).show();
            binding.progressCircular.setVisibility(View.GONE);
            return;
        }

        int repetitionCount = Integer.parseInt(repetitionCountString);

        if (repetitionCount <= 0) {
            Toast.makeText(getActivity(), "Repetition count should be greater than 0", Toast.LENGTH_SHORT).show();
            binding.progressCircular.setVisibility(View.GONE);
            return;
        }

        // Get the selected image from the ImageView
        Bitmap originalBitmap = null;

        if (imageView.getDrawable() != null) {
            originalBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        }

        if (originalBitmap == null) {
            binding.progressCircular.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a list of repeated images
        List<Bitmap> repeatedImages = new ArrayList<>();

        for (int i = 0; i < repetitionCount; i++) {
            repeatedImages.add(originalBitmap);
        }

        // Create a temporary directory to store images
        File tempDir = getActivity().getExternalCacheDir();
        if (tempDir == null) {
            Toast.makeText(getActivity(), "Unable to create a temporary directory", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save repeated images to files
        List<Uri> imageUris = new ArrayList<>();
        for (int i = 0; i < repeatedImages.size(); i++) {
            File imageFile = new File(tempDir, "image_" + i + ".png");
            Toast.makeText(getActivity(), "Image Processing item " + i, Toast.LENGTH_SHORT).show();
            saveBitmapToFile(repeatedImages.get(i), imageFile);
            imageUris.add(getUriForFile(imageFile));
        }

        // Create a ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Sharing Process Loading...");
        progressDialog.setCancelable(false); // Optional: Make it non-cancelable if needed
        progressDialog.show();


        // Use a Handler to dismiss the ProgressDialog after a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                binding.progressCircular.setVisibility(View.GONE);

                // Share images using Intent.ACTION_SEND
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(imageUris));
                sendIntent.setType("image/*");

                // Grant read permissions to other apps
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(sendIntent, "Share images"));

                // Clear the repetition count and image after sharing
                repetitionEditText.getText().clear();

            }
        }, repetitionCount); // Adjust the delay as needed
    }

    private Uri getUriForFile(File file) {
        return FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
    }

    private void saveBitmapToFile(Bitmap bitmap, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Uri> getUrisFromFiles(List<File> files) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (File file : files) {
            uris.add(Uri.fromFile(file));
        }
        return uris;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Add your code to clear the cache or perform cleanup operations here
        clearCache();
        binding = null;
    }

    private void clearCache() {
        // Implement your logic to clear the cache here
        // For example, if you are using a File cache, you can delete the cache directory
        File cacheDir = getActivity().getExternalCacheDir();
        if (cacheDir != null) {
            deleteRecursive(cacheDir);
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
}