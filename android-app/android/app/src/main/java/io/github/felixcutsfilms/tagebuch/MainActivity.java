package io.github.felixcutsfilms.tagebuch;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(FolderStoragePlugin.class); // muss vor super.onCreate stehen
        super.onCreate(savedInstanceState);
    }
}
