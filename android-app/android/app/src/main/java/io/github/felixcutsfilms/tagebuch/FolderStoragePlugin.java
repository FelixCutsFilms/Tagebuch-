package io.github.felixcutsfilms.tagebuch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResult;
import androidx.documentfile.provider.DocumentFile;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Lässt den Nutzer einen Ordner wählen (Storage Access Framework), merkt sich ihn dauerhaft
 * und schreibt Dateien (UTF-8) hinein.
 */
@CapacitorPlugin(name = "FolderStorage")
public class FolderStoragePlugin extends Plugin {
    private static final String PREFS = "folder_storage";
    private static final String KEY_URI = "tree_uri";

    private SharedPreferences prefs() {
        return getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Gespeicherter Ordner, falls die Berechtigung noch gültig ist. */
    private DocumentFile savedFolder() {
        String s = prefs().getString(KEY_URI, null);
        if (s == null) return null;
        Uri uri = Uri.parse(s);
        boolean granted = false;
        for (android.content.UriPermission p : getContext().getContentResolver().getPersistedUriPermissions()) {
            if (p.getUri().equals(uri) && p.isWritePermission()) { granted = true; break; }
        }
        if (!granted) return null;
        DocumentFile dir = DocumentFile.fromTreeUri(getContext(), uri);
        return (dir != null && dir.exists() && dir.canWrite()) ? dir : null;
    }

    private JSObject folderInfo(DocumentFile dir) {
        JSObject r = new JSObject();
        r.put("selected", dir != null);
        if (dir != null) r.put("name", dir.getName());
        return r;
    }

    @PluginMethod
    public void getFolder(PluginCall call) {
        call.resolve(folderInfo(savedFolder()));
    }

    @PluginMethod
    public void pickFolder(PluginCall call) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        startActivityForResult(call, intent, "pickResult");
    }

    @ActivityCallback
    private void pickResult(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null
                || result.getData().getData() == null) {
            call.reject("cancelled");
            return;
        }
        Uri uri = result.getData().getData();
        try {
            getContext().getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (Exception e) {
            call.reject("Berechtigung konnte nicht gespeichert werden: " + e.getMessage());
            return;
        }
        prefs().edit().putString(KEY_URI, uri.toString()).apply();
        call.resolve(folderInfo(savedFolder()));
    }

    @PluginMethod
    public void writeFile(PluginCall call) {
        String name = call.getString("name");
        String content = call.getString("content", "");
        if (name == null || name.isEmpty() || name.contains("/")) {
            call.reject("Ungültiger Dateiname");
            return;
        }
        DocumentFile dir = savedFolder();
        if (dir == null) {
            call.reject("no-folder");
            return;
        }
        try {
            DocumentFile file = dir.findFile(name);
            if (file == null) {
                String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                if (mime == null) mime = "application/octet-stream";
                file = dir.createFile(mime, name);
            }
            if (file == null) {
                call.reject("Datei konnte nicht angelegt werden");
                return;
            }
            try (OutputStream out = getContext().getContentResolver().openOutputStream(file.getUri(), "wt")) {
                if (out == null) {
                    call.reject("Datei konnte nicht geöffnet werden");
                    return;
                }
                out.write(content.getBytes(StandardCharsets.UTF_8));
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Schreiben fehlgeschlagen: " + e.getMessage());
        }
    }
}
