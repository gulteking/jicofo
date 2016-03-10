/*
 * Jicofo, the Jitsi Conference Focus.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.impl.protocol.xmpp;

import net.java.sip.communicator.impl.protocol.jabber.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jitsimeet.*;
import net.java.sip.communicator.service.protocol.*;

import net.java.sip.communicator.service.protocol.globalstatus.*;
import net.java.sip.communicator.util.*;
import org.jitsi.protocol.xmpp.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.muc.*;

/**
 * Stripped Smack implementation of {@link ChatRoomMember}.
 *
 * @author Pawel Domas
 */
public class ChatMemberImpl
    implements XmppChatMember
{
    /**
     * The logger
     */
    static final private Logger logger = Logger.getLogger(ChatMemberImpl.class);

    /**
     * The MUC nickname used by this member.
     */
    private final String nickname;

    /**
     * The chat room of the member.
     */
    private final ChatRoomImpl chatRoom;

    /**
     * Join order number
     */
    private final int joinOrderNumber;

    /**
     * Full MUC address:
     * room_name@muc.server.net/nickname
     */
    private final String address;

    /**
     * Stores the last <tt>Presence</tt> processed by this
     * <tt>ChatMemberImpl</tt>.
     */
    private Presence presence;

    private ChatRoomMemberRole role;

    /**
     * Stores video muted status if any.
     */
    private Boolean videoMuted;

    public ChatMemberImpl(String participant, ChatRoomImpl chatRoom,
        int joinOrderNumber)
    {
        this.address = participant;
        this.nickname = participant.substring(participant.lastIndexOf("/")+1);
        this.chatRoom = chatRoom;
        this.joinOrderNumber = joinOrderNumber;
    }

    @Override
    public ChatRoom getChatRoom()
    {
        return chatRoom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Presence getPresence()
    {
        return presence;
    }

    @Override
    public ProtocolProviderService getProtocolProvider()
    {
        return chatRoom.getParentProvider();
    }

    @Override
    public String getContactAddress()
    {
        return address;
    }

    @Override
    public String getName()
    {
        return nickname;
    }

    @Override
    public byte[] getAvatar()
    {
        return new byte[0];
    }

    @Override
    public Contact getContact()
    {
        return null;
    }

    @Override
    public ChatRoomMemberRole getRole()
    {
        if(this.role == null)
        {
            Occupant o = chatRoom.getOccupant(this);

            if(o == null)
                return ChatRoomMemberRole.GUEST;
            else
                this.role = ChatRoomJabberImpl
                    .smackRoleToScRole(o.getRole(), o.getAffiliation());
        }
        return this.role;
    }

    /**
     * Reset cached user role so that it will be refreshed when {@link
     * #getRole()} is called.
     */
    void resetCachedRole()
    {
        this.role = null;
    }

    @Override
    public void setRole(ChatRoomMemberRole role)
    {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public PresenceStatus getPresenceStatus()
    {
        return GlobalStatusEnum.ONLINE;
    }

    @Override
    public String getJabberID()
    {
        return chatRoom.getMemberJid(address);
    }

    @Override
    public int getJoinOrderNumber()
    {
        return joinOrderNumber;
    }

    @Override
    public Boolean hasVideoMuted()
    {
        return videoMuted;
    }

    /**
     * Does presence processing.
     *
     * @param presence the instance of <tt>Presence</tt> packet extension sent
     *                 by this chat member.
     */
    void processPresence(Presence presence)
    {
        this.presence = presence;

        VideoMutedExtension videoMutedExt
            = (VideoMutedExtension)
                presence.getExtension(
                    VideoMutedExtension.ELEMENT_NAME,
                    VideoMutedExtension.NAMESPACE);

        if (videoMutedExt != null)
        {
            Boolean newStatus = videoMutedExt.isVideoMuted();
            if (newStatus != videoMuted)
            {
                logger.debug(
                    getContactAddress() + " video muted: " + newStatus);

                videoMuted = newStatus;
            }
        }
    }
}
