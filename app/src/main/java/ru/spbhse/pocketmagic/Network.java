package ru.spbhse.pocketmagic;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public class Network {

    public class CallbackHandler extends RoomStatusUpdateCallback {

        @Override
        public void onRoomConnecting(@Nullable Room room) {
            Log.d("Pocket Magic", "Connection to room...");
            NetworkController.showMessage("Connection to room...");
            Network.this.room = room;
        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {
            Log.d("Pocket Magic", "New room found");
            NetworkController.showMessage("New room found");
            Network.this.room = room;
        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
            Log.d("Pocket Magic", "Receive an invitation to a new room");
            NetworkController.showMessage("Receive an invitation to a new room");
            Network.this.room = room;
        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
            Log.d("Pocket Magic", "Peer connection declined");
            NetworkController.showMessage("Peer connection declined");
        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
            Log.d("Pocket Magic", "Joined with peer connection");
            NetworkController.showMessage("Joined with peer connection");
            Network.this.room = room;
        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
            Log.d("Pocket Magic", "Left from peer connection");
            Network.this.leaveRoom();
        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            Log.d("Pocket Magic", "Connected to a new room");
            NetworkController.showMessage("Connected to a new room");
            Network.this.room = room;
        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            Log.d("Pocket Magic", "Disconnected from a room");
            Network.this.leaveRoom();
        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d("Pocket Magic", "Connected with a peer connection");
            NetworkController.showMessage("Connected with a peer connection");
            Network.this.room = room;
        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d("Pocket Magic", "Disconnected from a peer connection");
            Network.this.leaveRoom();
        }

        @Override
        public void onP2PConnected(@NonNull String s) {
            Log.d("Pocket Magic", "P2P connected");
            NetworkController.showMessage("P2P connected");
        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {
            Log.d("Pocket Magic", "P2P disconnected");
            Network.this.leaveRoom();
        }
    }

    public class MessageListener implements OnRealTimeMessageReceivedListener {

        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            Log.d("Pocket Magic","Received message");
            byte[] buf = realTimeMessage.getMessageData();

            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(buf))) {
                NetworkController.receiveSpell(in.readInt());
            } catch (IOException e) {
                Log.wtf("Pocket Magic", "Can't create DataInputStream!");
                e.printStackTrace();
            }
        }
    }

    public class CallbackUpdater extends RoomUpdateCallback {

        @Override
        public void onRoomCreated(int i, @Nullable Room room) {
            Log.d("Pocket Magic", "Room has been created");
            NetworkController.showMessage("Room has been created");
        }

        @Override
        public void onJoinedRoom(int i, @Nullable Room room) {
            Log.d("Pocket Magic", "Joined to room");
            NetworkController.showMessage("Joined to room");
        }

        @Override
        public void onLeftRoom(int i, @NonNull String s) {
            Log.d("Pocket Magic", "Left from room");
            NetworkController.showAlert("Disconnected");
        }

        @Override
        public void onRoomConnected(int i, @Nullable Room room) {
            if (room == null) {
                Log.wtf("Pocket Magic", "Null pointer as Room!");
                return;
            }
            Log.d("Pocket Magic", "Connected to room");

            Network.this.room = room;
            if (i == GamesCallbackStatusCodes.OK) {
                Log.d("Pocket Magic", "Connected");
            } else {
                Log.e("Pocket Magic", "Error while connecting");
                return;
            }

            Games.getPlayersClient(NetworkController.getContext(), account)
                    .getCurrentPlayerId()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String myPlayerId) {
                            Network.this.playerID = Network.this.room.getParticipantId(myPlayerId);
                            Log.d("Pocket Magic", "Received playerID");
                            NetworkController.startGame();
                        }
                    });
        }
    }

    private InvitationCallback invitationCallback = new InvitationCallback() {

        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            Log.wtf("Pocket Magic", "Got invitation!");
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {
            Log.wtf("Pocket Magic", "Invitation removed");
        }
    };

    private RoomConfig roomConfig;
    private Room room;
    private GoogleSignInAccount account;
    private RealTimeMultiplayerClient client;
    private String playerID;

    public Network(GoogleSignInAccount account) {
        this.account = account;
        client = Games.getRealTimeMultiplayerClient(NetworkController.getContext(), account);
        GamesClient gamesClient = Games.getGamesClient(NetworkController.getContext(), account);
        gamesClient
                .getActivationHint()
                .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                    @Override
                    public void onSuccess(Bundle hint) {
                        if (hint != null) {
                            Invitation invitation =
                                    hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                            if (invitation != null && invitation.getInvitationId() != null) {
                                // retrieve and cache the invitation ID
                                Log.d("Pocket Magic", "Connection hint has a room invite!");
                                acceptInviteToRoom(invitation.getInvitationId());
                            }
                        }
                    }
                });

        Games
                .getPlayersClient(NetworkController.getContext(), account)
                .getCurrentPlayer()
                .addOnSuccessListener(new OnSuccessListener<Player>() {
                    @Override
                    public void onSuccess(Player player) {
                        playerID = player.getPlayerId();
                        findAndStartGame();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        NetworkController.showAlert("Can't get your player ID");
                    }
                });
    }

    public void findAndStartGame() {
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        roomConfig = RoomConfig.builder(this.new CallbackUpdater())
                .setOnMessageReceivedListener(this.new MessageListener())
                .setRoomStatusUpdateCallback(this.new CallbackHandler())
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        client.create(roomConfig);
    }

    private void acceptInviteToRoom(String invitationId) {
        Log.d("Pocket Magic", "Accepting invitation: " + invitationId);

        roomConfig = RoomConfig.builder(this.new CallbackUpdater())
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(this.new MessageListener())
                .setRoomStatusUpdateCallback(this.new CallbackHandler())
                .build();

        client.join(roomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Pocket Magic", "Room Joined Successfully!");
                    }
                });
    }

    private void leaveRoom() {
        if (room != null) {
            Games.getRealTimeMultiplayerClient(NetworkController.getContext(),
                    account).leave(roomConfig, room.getRoomId());
            room = null;
        }
    }

    public String getOpponentName() {
        for (String id: room.getParticipantIds()) {
            if (!id.equals(playerID)) {
                return room.getParticipant(id).getDisplayName();
            }
        }
        return "Your opponent";
    }

    public void sendMessage(byte[] message) {
        if (playerID == null || room == null) {
            Log.e("Pocket Magic", "Cannot send message before initialization");
            return;
        }

        for (String id: room.getParticipantIds()) {
            if (!id.equals(playerID)) {
                client.sendReliableMessage(
                        message, room.getRoomId(),
                        id,
                        new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int i, int i1, String s) {
                                /* Do nothing */
                            }
                        });
            }
        }
    }
}
