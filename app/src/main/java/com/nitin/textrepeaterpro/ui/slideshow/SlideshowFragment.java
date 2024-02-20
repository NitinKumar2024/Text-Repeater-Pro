package com.nitin.textrepeaterpro.ui.slideshow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.nitin.textrepeaterpro.R;
import com.nitin.textrepeaterpro.databinding.FragmentSlideshowBinding;

public class SlideshowFragment extends Fragment {

    private EditText userInputEditText;
    private RecyclerView fontRecyclerView;


    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userInputEditText = binding.userInputEditText;
        fontRecyclerView = binding.fontRecyclerView;

        List<String> fontStyles = getFontStyles();
        FontAdapter fontAdapter = new FontAdapter(fontStyles);
        fontRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fontRecyclerView.setAdapter(fontAdapter);

        // Add TextWatcher to dynamically update the font styles
        userInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                fontAdapter.setUserInput(editable.toString());
            }
        });





        return root;
    }

    private List<String> getFontStyles() {
        // Include a few font styles for illustration
        List<String> fontStyles = new ArrayList<>();
        fontStyles.add("Normal");
        fontStyles.add("Bold");
        fontStyles.add("Italic");
        fontStyles.add("Underline");
        fontStyles.add("Strikethrough");
        fontStyles.add("Causal");

        return fontStyles;
    }

    private class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> {

        private List<String> fontStyles;

        private String userInput = ""; // Store the user input

        FontAdapter(List<String> fontStyles) {
            this.fontStyles = fontStyles;
        }



        public void setUserInput(String userInput) {
            this.userInput = userInput;
            notifyDataSetChanged(); // Update the RecyclerView when the user input changes
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_style, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
           // String fontStyle = fontStyles.get(position);
           // holder.fontStyleTextView.setText(fontStyle);
            String fontStyle = fontStyles.get(position);
            holder.fontStyleTextView.setText(applyStyle(fontStyle, userInput));


            holder.copyImageView.setOnClickListener(v -> {
                // Copy the styled text to clipboard
                String inputText = userInputEditText.getText().toString();
                SpannableString styledText = applyStyle(fontStyle, userInput);
                copyToClipboard(styledText);
                Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            });

            holder.shareImageView.setOnClickListener(v -> {
                // Share the styled text
                String inputText = userInputEditText.getText().toString();
                SpannableString styledText = applyStyle(fontStyle, inputText);
                shareText(styledText);
            });
        }

        @Override
        public int getItemCount() {
            return fontStyles.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView fontStyleTextView;
            ImageView copyImageView;
            ImageView shareImageView;

            ViewHolder(View itemView) {
                super(itemView);
                fontStyleTextView = itemView.findViewById(R.id.fontStyleTextView);
                copyImageView = itemView.findViewById(R.id.copyImageView);
                shareImageView = itemView.findViewById(R.id.shareImageView);
            }
        }
    }

    private SpannableString applyStyle(String style, String text) {
        SpannableString spannableString = new SpannableString(text);
        switch (style) {
            case "Bold":
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "Italic":
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "Underline":
                spannableString.setSpan(new android.text.style.UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "Strikethrough":
                spannableString.setSpan(new android.text.style.StrikethroughSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            // Add more cases for other styles as needed

        }
        return spannableString;
    }
    // Add more font styles



    private void copyToClipboard(SpannableString text) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("StyledText", text));
        }
    }

    private void shareText(SpannableString text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}