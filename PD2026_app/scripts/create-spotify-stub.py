#!/usr/bin/env python3
"""
Creates a minimal Spotify App Remote SDK stub AAR for CI compilation.

The stub provides only the class signatures needed to compile feature-spotify;
it contains no real Spotify SDK logic and is safe to commit to git.

Usage:
    python3 scripts/create-spotify-stub.py
Run from the PD2026_app root directory.
"""
import os
import sys
import zipfile
import subprocess
import shutil
import tempfile
import textwrap

# __file__ is  <repo>/PD2026_app/scripts/create-spotify-stub.py
# dirname x2  → <repo>/PD2026_app/
LIBS_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "libs")
# Fallback when running from an unusual working directory
if not os.path.isabs(LIBS_DIR) or not os.path.isdir(os.path.dirname(LIBS_DIR)):
    LIBS_DIR = os.path.join(os.getcwd(), "libs")

AAR_NAME = "spotify-app-remote-release-0.8.0.aar"

# ---------------------------------------------------------------------------
# Java stub sources — covers every class imported by feature-spotify Kotlin code
# ---------------------------------------------------------------------------
JAVA_STUBS = {
    "com/spotify/protocol/client/EventCallback.java": """
        package com.spotify.protocol.client;
        public interface EventCallback<T> {
            void onEvent(T data);
        }
    """,
    "com/spotify/protocol/client/CallResult.java": """
        package com.spotify.protocol.client;
        public interface CallResult<T> {
        }
    """,
    "com/spotify/protocol/client/Subscription.java": """
        package com.spotify.protocol.client;
        public interface Subscription<T> {
            Subscription<T> setEventCallback(EventCallback<T> callback);
            void cancel();
        }
    """,
    "com/spotify/protocol/types/Artist.java": """
        package com.spotify.protocol.types;
        public class Artist {
            public String name = "";
        }
    """,
    "com/spotify/protocol/types/Track.java": """
        package com.spotify.protocol.types;
        public class Track {
            public Artist artist = new Artist();
            public String name = "";
        }
    """,
    "com/spotify/protocol/types/PlayerState.java": """
        package com.spotify.protocol.types;
        public class PlayerState {
            public Track track = null;
            public boolean isPaused = false;
        }
    """,
    "com/spotify/android/appremote/api/ConnectionParams.java": """
        package com.spotify.android.appremote.api;
        public class ConnectionParams {
            private ConnectionParams() {}
            public static class Builder {
                public Builder(String clientId) {}
                public Builder setRedirectUri(String redirectUri) { return this; }
                public Builder showAuthView(boolean show) { return this; }
                public ConnectionParams build() { return new ConnectionParams(); }
            }
        }
    """,
    "com/spotify/android/appremote/api/Connector.java": """
        package com.spotify.android.appremote.api;
        public interface Connector {
            interface ConnectionListener {
                void onConnected(SpotifyAppRemote appRemote);
                void onFailure(Throwable throwable);
            }
        }
    """,
    "com/spotify/android/appremote/api/PlayerApi.java": """
        package com.spotify.android.appremote.api;
        import com.spotify.protocol.client.CallResult;
        import com.spotify.protocol.client.Subscription;
        import com.spotify.protocol.types.PlayerState;
        public interface PlayerApi {
            CallResult<Void> play(String uri);
            Subscription<PlayerState> subscribeToPlayerState();
        }
    """,
    # android.content.Context is replaced with Object so javac needs no Android SDK
    "com/spotify/android/appremote/api/SpotifyAppRemote.java": """
        package com.spotify.android.appremote.api;
        public class SpotifyAppRemote {
            public PlayerApi getPlayerApi() { return null; }
            public static void connect(
                    Object context,
                    ConnectionParams params,
                    Connector.ConnectionListener listener) {}
            public static void disconnect(SpotifyAppRemote appRemote) {}
            public static boolean isSpotifyInstalled(Object context) { return false; }
        }
    """,
}

ANDROID_MANIFEST = (
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<manifest package="com.spotify.android.appremote"/>\n'
)


def create_stub() -> None:
    tmp = tempfile.mkdtemp(prefix="spotify-stub-")
    try:
        src_dir = os.path.join(tmp, "src")
        classes_dir = os.path.join(tmp, "classes")
        os.makedirs(classes_dir)

        # Write Java sources
        for rel_path, content in JAVA_STUBS.items():
            full = os.path.join(src_dir, rel_path)
            os.makedirs(os.path.dirname(full), exist_ok=True)
            with open(full, "w") as f:
                f.write(textwrap.dedent(content).strip() + "\n")

        # Collect .java files
        java_srcs = []
        for root, _, files in os.walk(src_dir):
            for name in files:
                if name.endswith(".java"):
                    java_srcs.append(os.path.join(root, name))

        # Compile with javac (JDK 11 bytecode; no Android SDK needed)
        result = subprocess.run(
            ["javac", "--release", "11", "-d", classes_dir] + java_srcs,
            capture_output=True,
            text=True,
        )
        if result.returncode != 0:
            print("javac stderr:\n" + result.stderr, file=sys.stderr)
            sys.exit(result.returncode)

        # Build classes.jar
        classes_jar = os.path.join(tmp, "classes.jar")
        with zipfile.ZipFile(classes_jar, "w", zipfile.ZIP_DEFLATED) as jar:
            for root, _, files in os.walk(classes_dir):
                for name in files:
                    full = os.path.join(root, name)
                    jar.write(full, os.path.relpath(full, classes_dir))

        # Build AndroidManifest.xml
        manifest_path = os.path.join(tmp, "AndroidManifest.xml")
        with open(manifest_path, "w") as f:
            f.write(ANDROID_MANIFEST)

        # Pack AAR (= zip of manifest + classes.jar)
        os.makedirs(LIBS_DIR, exist_ok=True)
        aar_path = os.path.join(LIBS_DIR, AAR_NAME)
        with zipfile.ZipFile(aar_path, "w", zipfile.ZIP_DEFLATED) as aar:
            aar.write(manifest_path, "AndroidManifest.xml")
            aar.write(classes_jar, "classes.jar")

        print(f"✓ Spotify SDK stub created: {aar_path}")

    finally:
        shutil.rmtree(tmp, ignore_errors=True)


if __name__ == "__main__":
    create_stub()
