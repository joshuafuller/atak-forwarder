package com.paulmandal.atak.forwarder.comm.protobuf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atakmap.coremap.cot.event.CotAttribute;
import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.cot.event.CotPoint;
import com.atakmap.coremap.maps.time.CoordinatedTime;
import com.google.protobuf.InvalidProtocolBufferException;
import com.paulmandal.atak.forwarder.cotutils.CotMessageTypes;
import com.paulmandal.atak.forwarder.protobufs.ProtobufChat;
import com.paulmandal.atak.forwarder.protobufs.ProtobufChatGroup;
import com.paulmandal.atak.forwarder.protobufs.ProtobufContact;
import com.paulmandal.atak.forwarder.protobufs.ProtobufCotEvent;
import com.paulmandal.atak.forwarder.protobufs.ProtobufDetail;
import com.paulmandal.atak.forwarder.protobufs.ProtobufGroup;
import com.paulmandal.atak.forwarder.protobufs.ProtobufHierarchy;
import com.paulmandal.atak.forwarder.protobufs.ProtobufLink;
import com.paulmandal.atak.forwarder.protobufs.ProtobufModel;
import com.paulmandal.atak.forwarder.protobufs.ProtobufRemarks;
import com.paulmandal.atak.forwarder.protobufs.ProtobufServerDestination;
import com.paulmandal.atak.forwarder.protobufs.ProtobufTakv;
import com.paulmandal.atak.forwarder.protobufs.ProtobufTrack;
import com.paulmandal.atak.forwarder.protobufs.ProtobufUnderscoreGroup;

import org.apache.commons.lang.ArrayUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MinimalCotProtobufConverter {
    private static final String TAG = "ATAKDBG." + MinimalCotProtobufConverter.class.getSimpleName();

    private static final int LONG_INT_LENGTH = 64;

    /**
     * CotDetail fields
     */
    private static final String KEY_DETAIL = "detail";
    private static final String KEY_CONTACT = "contact";
    private static final String KEY_UNDERSCORED_GROUP = "__group";
    private static final String KEY_GROUP = "group";
    private static final String KEY_PRECISIONLOCATION = "precisionlocation";
    private static final String KEY_COLOR = "color";
    private static final String KEY_ARCHIVE = "archive";
    private static final String KEY_STATUS = "status";
    private static final String KEY_TAKV = "takv";
    private static final String KEY_TRACK = "track";
    private static final String KEY_REMARKS = "remarks";
    private static final String KEY_LINK = "link";
    private static final String KEY_USER_ICON = "usericon";
    private static final String KEY_CHAT = "__chat";
    private static final String KEY_CHAT_GROUP = "chatgrp";
    private static final String KEY_HIERARCHY = "hierarchy";
    private static final String KEY_SERVER_DESTINATION = "__serverdestination";
    private static final String KEY_MODEL = "model";
    private static final String KEY_LABELS_ON = "labels_on";
    private static final String KEY_HEIGHT_UNIT = "height_unit";
    private static final String KEY_HEIGHT = "height";

    // Contact
    private static final String KEY_CALLSIGN = "callsign";
    private static final String KEY_ENDPOINT = "endpoint";

    // Group
    private static final String KEY_NAME = "name";
    private static final String KEY_ROLE = "role";

    // PrecisionLocation
    private static final String KEY_GEOPOINTSRC = "geopointsrc";
    private static final String KEY_ALTSRC = "altsrc";

    // Status
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_READINESS = "readiness";

    // TakV
    private static final String KEY_DEVICE = "device";
    private static final String KEY_OS = "os";
    private static final String KEY_PLATFORM = "platform";
    private static final String KEY_VERSION = "version";

    // Track
    private static final String KEY_COURSE = "course";
    private static final String KEY_SPEED = "speed";

    // UserIcon
    private static final String KEY_ICON_SET_PATH = "iconsetpath";

    // Link
    private static final String KEY_UID = "uid";
    private static final String KEY_TYPE = "type";
    private static final String KEY_PARENT_CALLSIGN = "parent_callsign";
    private static final String KEY_RELATION = "relation";
    private static final String KEY_PRODUCTION_TIME = "production_time";

    // UID
    private static final String KEY_DROID = "Droid";

    // Color
    private static final String KEY_ARGB = "argb";
    private static final String KEY_VALUE = "value";

    // Chat
    private static final String KEY_PARENT = "parent";
    private static final String KEY_GROUP_OWNER = "groupOwner";
    private static final String KEY_CHATROOM = "chatroom";
    private static final String KEY_ID = "id";
    private static final String KEY_SENDER_CALLSIGN = "senderCallsign";

    // Remarks
    private static final String KEY_SOURCE = "source";
    private static final String KEY_TO = "to";
    private static final String KEY_TIME = "time";

    // Server Destination
    private static final String KEY_DESTINATIONS = "destinations";

    // Height
    private static final String KEY_UNIT = "unit";

    /**
     * Special Values
     */
    private static final String VALUE_TRUE = "true";
    private static final String FAKE_ENDPOINT_ADDRESS = "10.254.254.254:4242:tcp";
    private static final String DEFAULT_CHAT_PORT_AND_PROTO = ":4242:tcp";
    private static final String GEOCHAT_MARKER = "GeoChat";

    private static final String VALUE_USER_GROUPS = "UserGroups";
    private static final String USER_GROUPS_SUBSTITUTION_MARKER = "#m";

    private static final String VALUE_GROUPS = "Groups";
    private static final String GROUPS_SUBSTITUION_MARKER = "#g";

    /**
     * Substitution Markers
     */
    private static final String CHATROOM_SUBSTITUTION_MARKER = "#c";
    private static final String UID_SUBSTITUTION_MARKER = "#u";
    private static final String ID_SUBSTITUTION_MARKER = "#i";
    private static final String SENDER_CALLSIGN_SUBSTITUION_MARKER = "#s";

    /**
     * Mappings
     */
    private static final String[] MAPPING_HOW = {
            "h-e",
            "h-g-i-g-o",
            "m-g"
    };

    private static final String[] MAPPING_ALTSRC_AND_GEOPOINTSRC = {
            "DTED0",
            "DTED1",
            "DTED2",
            "DTED3",
            "LIDAR",
            "PFI",
            "USER",
            "???",
            "GPS",
            "SRTM1",
            "COT",
            "PRI",
            "CALC",
            "ESTIMATED",
            "RTK",
            "DGPS",
            "GPS_PPS"
    };

    private static final String[] MAPPING_GROUP_ROLE = {
            "Team Member",
            "Team Lead",
            "HQ",
            "Sniper",
            "Medic",
            "Forward Observer",
            "RTO",
            "K9"
    };

    private static final String[] MAPPING_HEIGHT_UNIT = {
            "kilometers",
            "meters",
            "miles",
            "yards",
            "feet",
            "nautical miles"
    };

    private final long mStartOfYearMs;

    public MinimalCotProtobufConverter(long startOfYearMs) {
        mStartOfYearMs = startOfYearMs;
    }

    public byte[] toByteArray(CotEvent cotEvent) throws MappingNotFoundException, UnknownDetailFieldException {
        ProtobufCotEvent.MinimalCotEvent cotEventProtobuf = toCotEventProtobuf(cotEvent);
        return cotEventProtobuf.toByteArray();
    }

    public CotEvent toCotEvent(byte[] cotProtobuf) {
        CotEvent cotEvent = null;

        try {
            ProtobufCotEvent.MinimalCotEvent protoCotEvent = ProtobufCotEvent.MinimalCotEvent.parseFrom(cotProtobuf);

            CustomBytesFields customBytesFields = unpackCustomBytes(protoCotEvent.getCustomBytes());
            CustomBytesExtFields customBytesExtFields = unpackCustomBytesExt(protoCotEvent.getCustomBytesExt());

            cotEvent = new CotEvent();

            SubsitutionValues subsitutionValues = new SubsitutionValues();
            String uid = protoCotEvent.getUid();
            if (uid.startsWith(GEOCHAT_MARKER)) {
                String[] geoChatSplit = uid.split("\\.");
                subsitutionValues.uidFromGeoChat = geoChatSplit[1];
                subsitutionValues.chatroomFromGeoChat = geoChatSplit[2];
            }
            cotEvent.setUID(uid);
            cotEvent.setType(protoCotEvent.getType());
            cotEvent.setTime(customBytesFields.time);
            cotEvent.setStart(customBytesFields.time);
            cotEvent.setStale(customBytesFields.stale);
            cotEvent.setHow(customBytesExtFields.how);
            cotEvent.setDetail(cotDetailFromProtoDetail(protoCotEvent.getDetail(), cotEvent, customBytesExtFields, subsitutionValues));
            cotEvent.setPoint(new CotPoint(protoCotEvent.getLat(), protoCotEvent.getLon(), customBytesFields.hae, protoCotEvent.getCe(), protoCotEvent.getLe()));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return cotEvent;
    }

    /**
     * toByteArray
     */
    private ProtobufCotEvent.MinimalCotEvent toCotEventProtobuf(CotEvent cotEvent) throws MappingNotFoundException, UnknownDetailFieldException {
        ProtobufCotEvent.MinimalCotEvent.Builder builder = ProtobufCotEvent.MinimalCotEvent.newBuilder();

        SubsitutionValues subsitutionValues = new SubsitutionValues();
        String uid = cotEvent.getUID();
        if (uid != null) {
            builder.setUid(uid);
            if (uid.startsWith(GEOCHAT_MARKER)) {
                String[] geoChatSplit = uid.split("\\.");
                subsitutionValues.uidFromGeoChat = geoChatSplit[1];
                subsitutionValues.chatroomFromGeoChat = geoChatSplit[2];
            }
        }

        if (cotEvent.getType() != null) {
            builder.setType(cotEvent.getType());
        }

        CotPoint cotPoint = cotEvent.getCotPoint();

        if (cotPoint != null) {
            builder.setLat(cotPoint.getLat());
            builder.setLon(cotPoint.getLon());
            builder.setCe((int)cotPoint.getCe());
            builder.setLe((int)cotPoint.getLe());
        }

        double hae = cotPoint != null ? cotPoint.getHae() : 0;
        builder.setCustomBytes(packCustomBytes(cotEvent.getTime(), cotEvent.getStale(), hae));

        String geoPointSrc = null;
        String altSrc = null;
        CotDetail precisionLocation = cotEvent.getDetail().getFirstChildByName(0, KEY_PRECISIONLOCATION);
        if (precisionLocation != null) {
            geoPointSrc = precisionLocation.getAttribute(KEY_GEOPOINTSRC);
            altSrc = precisionLocation.getAttribute(KEY_ALTSRC);
        }

        String groupRole = null;
        CotDetail group = cotEvent.getDetail().getFirstChildByName(0, KEY_UNDERSCORED_GROUP);
        if (group != null) {
            groupRole = group.getAttribute(KEY_ROLE);
        }

        Integer battery = null;
        Boolean readiness = null;
        CotDetail status = cotEvent.getDetail().getFirstChildByName(0, KEY_STATUS);
        if (status != null) {
            String batteryStr = status.getAttribute(KEY_BATTERY);
            if (batteryStr != null) {
                battery = Integer.parseInt(batteryStr);
            }

            String readinessStr = status.getAttribute(KEY_READINESS);
            if (readinessStr != null) {
                readiness = Boolean.parseBoolean(readinessStr);
            }
        }

        Boolean labelsOn = null;
        CotDetail labelsOnDetail = cotEvent.getDetail().getFirstChildByName(0, KEY_LABELS_ON);
        if (labelsOnDetail != null) {
            String valueStr = labelsOnDetail.getAttribute(KEY_VALUE);
            if (valueStr != null) {
                labelsOn = Boolean.parseBoolean(valueStr);
            }
        }

        Integer heightUnit = null;
        Integer heightValue = null;
        CotDetail heightUnitDetail = cotEvent.getDetail().getFirstChildByName(0, KEY_HEIGHT_UNIT);
        if (heightUnitDetail != null) {
            String heightUnitStr = heightUnitDetail.getInnerText();
            if (heightUnitStr != null) {
                heightUnit = Integer.parseInt(heightUnitStr);
            }
        }

        CotDetail heightDetail = cotEvent.getDetail().getFirstChildByName(0, KEY_HEIGHT);
        if (heightDetail != null) {
            String heightUnitStr = heightDetail.getAttribute(KEY_UNIT);
            if (heightUnitStr != null) {
                heightUnit = findMappingForArray("height.unit", MAPPING_HEIGHT_UNIT, heightUnitStr);
            }

            String heightValueInnerText = heightDetail.getInnerText();
            if (heightValueInnerText != null) {
                double heightValueDouble = Double.parseDouble(heightValueInnerText);
                heightValue = (int)heightValueDouble;
            }

            String heightValueStr = heightDetail.getAttribute(KEY_VALUE);
            if (heightValueStr != null) {
                double heightValueDouble = Double.parseDouble(heightValueStr);
                heightValue = (int)heightValueDouble;
            }
        }

        builder.setCustomBytesExt(packCustomBytesExt(cotEvent.getHow(), geoPointSrc, altSrc, groupRole, battery, readiness, labelsOn, heightUnit, heightValue));

        builder.setDetail(toDetail(cotEvent.getDetail(), subsitutionValues));

        return builder.build();
    }

    private ProtobufDetail.MinimalDetail toDetail(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufDetail.MinimalDetail.Builder builder = ProtobufDetail.MinimalDetail.newBuilder();

        if (cotDetail.getElementName().equals(KEY_DETAIL)) { // TODO: do we need this check?
            for (CotDetail innerDetail : cotDetail.getChildren()) {
                switch (innerDetail.getElementName()) {
                    case KEY_CONTACT:
                        builder.setContact(toContact(innerDetail, subsitutionValues));
                        break;
                    case KEY_UNDERSCORED_GROUP:
                        builder.setGroup(toUnderscoreGroup(innerDetail, subsitutionValues));
                        break;
                    case KEY_TAKV:
                        builder.setTakv(toTakv(innerDetail));
                        break;
                    case KEY_TRACK:
                        builder.setTrack(toTrack(innerDetail));
                        break;
                    case KEY_REMARKS:
                        builder.setRemarks(toRemarks(innerDetail, subsitutionValues));
                        break;
                    case KEY_LINK:
                        builder.setLink(toLink(innerDetail, subsitutionValues));
                        break;
                    case KEY_CHAT:
                        builder.setChat(toChat(innerDetail, subsitutionValues));
                        break;
                    case KEY_SERVER_DESTINATION:
                        builder.setServerDestination(toServerDestination(innerDetail, subsitutionValues));
                        break;
                    case KEY_LABELS_ON:
                        toLabelsOn(innerDetail);
                        break;
                    case KEY_HEIGHT_UNIT:
                        toHeightUnit(innerDetail);
                        break;
                    case KEY_HEIGHT:
                        toHeight(innerDetail);
                        break;
                    case KEY_MODEL:
                        builder.setModel(toModel(innerDetail));
                        break;
                    case KEY_USER_ICON:
                        builder.setIconSetPath(innerDetail.getAttribute(KEY_ICON_SET_PATH));
                        break;
                    case KEY_COLOR:
                        toColor(innerDetail);
                        break;
                    case KEY_ARCHIVE:
                        toArchive(innerDetail);
                        break;
                    case KEY_STATUS:
                        toStatus(innerDetail);
                        break;
                    case KEY_UID:
                        toUid(innerDetail);
                        break;
                    case KEY_PRECISIONLOCATION:
                        toPrecisionLocation(innerDetail);
                        break;
                    default:
                        throw new UnknownDetailFieldException("Don't know how to handle detail subobject: " + innerDetail.getElementName());
                }
            }
        }
        return builder.build();
    }

    private ProtobufContact.MinimalContact toContact(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufContact.MinimalContact.Builder builder = ProtobufContact.MinimalContact.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_CALLSIGN:
                    builder.setCallsign(attribute.getValue());
                    break;
                case KEY_ENDPOINT:
                    try {
                        String[] connectionStrSplit = attribute.getValue().split(":");
                        byte[] endpointAddrAsBytes = InetAddress.getByName(connectionStrSplit[0]).getAddress();
                        int endpointAddr = new BigInteger(endpointAddrAsBytes).intValue();
                        builder.setEndpointAddr(endpointAddr);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    break;
                case KEY_UID:
                    String uid = attribute.getValue();
                    if (uid.equals(subsitutionValues.uidFromGeoChat)) {
                        uid = UID_SUBSTITUTION_MARKER;
                    } else if (subsitutionValues.uidsFromChatGroup.contains(uid)) {
                        uid = UID_SUBSTITUTION_MARKER + subsitutionValues.uidsFromChatGroup.indexOf(uid);
                    }
                    builder.setUid(uid);
                    break;
                case KEY_NAME:
                    String name = attribute.getValue();
                    if (name.equals(subsitutionValues.senderCallsignFromChat)) {
                        name = SENDER_CALLSIGN_SUBSTITUION_MARKER;
                    }
                    builder.setName(name);
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: contact." + attribute.getName());
            }
        }
        return builder.build();
    }

    private ProtobufUnderscoreGroup.MinimalUnderscoreGroup toUnderscoreGroup(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufUnderscoreGroup.MinimalUnderscoreGroup.Builder builder = ProtobufUnderscoreGroup.MinimalUnderscoreGroup.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_NAME:
                    builder.setName(attribute.getValue());
                    break;
                case KEY_ROLE:
                    // Do nothing, we pack this field into bits
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: __group." + attribute.getName());
            }
        }
        return builder.build();
    }

    private ProtobufGroup.MinimalGroup toGroup(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufGroup.MinimalGroup.Builder builder = ProtobufGroup.MinimalGroup.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_NAME:
                    String name = attribute.getValue();
                    if (name.equals(subsitutionValues.chatroomFromGeoChat)) {
                        name = CHATROOM_SUBSTITUTION_MARKER;
                    } else if (name.equals(VALUE_GROUPS)) {
                        name = GROUPS_SUBSTITUION_MARKER;
                    }
                    builder.setName(name);
                    break;
                case KEY_UID:
                    String uid = attribute.getValue();
                    if (uid.equals(subsitutionValues.idFromChat)) {
                        uid = ID_SUBSTITUTION_MARKER;
                    } else if (uid.equals(VALUE_USER_GROUPS)) {
                        uid = USER_GROUPS_SUBSTITUTION_MARKER;
                    }
                    builder.setUid(uid);
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: group." + attribute.getName());
            }
        }

        List<CotDetail> children = cotDetail.getChildren();
        for (CotDetail child : children) {
            switch (child.getElementName()) {
                case KEY_GROUP:
                    builder.setGroup(toGroup(child, subsitutionValues));
                    break;
                case KEY_CONTACT:
                    builder.addContact(toContact(child, subsitutionValues));
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle child detail object: group." + child.getElementName());
            }
        }
        return builder.build();
    }

    private ProtobufTakv.MinimalTakv toTakv(CotDetail cotDetail) throws UnknownDetailFieldException {
        ProtobufTakv.MinimalTakv.Builder builder = ProtobufTakv.MinimalTakv.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_DEVICE:
                    builder.setDevice(attribute.getValue());
                    break;
                case KEY_OS:
                    builder.setOs(Integer.parseInt(attribute.getValue()));
                    break;
                case KEY_PLATFORM:
                    builder.setPlatform(attribute.getValue());
                    break;
                case KEY_VERSION:
                    builder.setVersion(attribute.getValue());
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: takv." + attribute.getName());
            }
        }
        return builder.build();
    }

    private ProtobufTrack.MinimalTrack toTrack(CotDetail cotDetail) throws UnknownDetailFieldException {
        ProtobufTrack.MinimalTrack.Builder builder = ProtobufTrack.MinimalTrack.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_COURSE:
                    builder.setCourse(Double.parseDouble(attribute.getValue()));
                    break;
                case KEY_SPEED:
                    builder.setSpeed(Double.parseDouble(attribute.getValue()));
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: track." + attribute.getName());
            }
        }
        return builder.build();
    }

    private ProtobufRemarks.MinimalRemarks toRemarks(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufRemarks.MinimalRemarks.Builder builder = ProtobufRemarks.MinimalRemarks.newBuilder();
        String remarks = cotDetail.getInnerText();
        if (remarks != null) {
            builder.setRemarks(remarks);
        }
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_SOURCE:
                    String source = attribute.getValue();
                    if (source.contains(subsitutionValues.uidFromGeoChat)) {
                        builder.setSource(source.replace(subsitutionValues.uidFromGeoChat, UID_SUBSTITUTION_MARKER));
                    } else {
                        builder.setSource(source);
                    }
                    break;
                case KEY_TO:
                    builder.setTo(attribute.getValue());
                    break;
                case KEY_TIME:
                    try {
                        long time = CoordinatedTime.fromCot(attribute.getValue()).getMilliseconds();
                        long sinceStartOfYear = (time - mStartOfYearMs) / 1000;
                        builder.setTime((int)sinceStartOfYear);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: remarks." + attribute.getName());
            }
        }
        return builder.build();
    }

    private ProtobufLink.MinimalLink toLink(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufLink.MinimalLink.Builder builder = ProtobufLink.MinimalLink.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_UID:
                    String uid = attribute.getValue();
                    if (uid.equals(subsitutionValues.uidFromGeoChat)) {
                        uid = UID_SUBSTITUTION_MARKER;
                    }
                    builder.setUid(uid);
                    break;
                case KEY_TYPE:
                    builder.setType(attribute.getValue());
                    break;
                case KEY_PARENT_CALLSIGN:
                    builder.setParentCallsign(attribute.getValue());
                    break;
                case KEY_RELATION:
                    builder.setRelation(attribute.getValue());
                    break;
                case KEY_PRODUCTION_TIME:
                    try {
                        long productionTime = CoordinatedTime.fromCot(attribute.getValue()).getMilliseconds();
                        long sinceStartOfYear = (productionTime - mStartOfYearMs) / 1000;
                        builder.setProductionTime((int) sinceStartOfYear);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: link." + attribute.getName());
            }
        }
        return builder.build();
    }

    private ProtobufChat.MinimalChat toChat(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufChat.MinimalChat.Builder builder = ProtobufChat.MinimalChat.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_PARENT:
                    String parent = attribute.getValue();
                    if (parent.equals(VALUE_USER_GROUPS)) {
                        parent = USER_GROUPS_SUBSTITUTION_MARKER;
                    }
                    builder.setParent(parent);
                    break;
                case KEY_GROUP_OWNER:
                    builder.setGroupOwner(1 << 1 | (attribute.getValue().equals(VALUE_TRUE) ? 1 : 0));
                    break;
                case KEY_CHATROOM:
                    String chatroom = attribute.getValue();
                    if (chatroom.equals(subsitutionValues.chatroomFromGeoChat)) {
                        chatroom = CHATROOM_SUBSTITUTION_MARKER;
                    }
                    builder.setChatroom(chatroom);
                    break;
                case KEY_ID:
                    subsitutionValues.idFromChat = attribute.getValue();
                    builder.setId(subsitutionValues.idFromChat);
                    break;
                case KEY_SENDER_CALLSIGN:
                    subsitutionValues.senderCallsignFromChat = attribute.getValue();
                    builder.setSenderCallsign(subsitutionValues.senderCallsignFromChat);
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: chat." + attribute.getName());
            }
        }

        List<CotDetail> children = cotDetail.getChildren();
        for (CotDetail childDetail : children) {
            switch (childDetail.getElementName()) {
                case KEY_CHAT_GROUP:
                    builder.setChatGroup(toChatGroup(childDetail, subsitutionValues));
                    break;
                case KEY_HIERARCHY:
                    builder.setHierarchy(toHierarchy(childDetail, subsitutionValues));
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle child object: chat." + childDetail.getElementName());
            }
        }
        return builder.build();
    }

    private ProtobufServerDestination.MinimalServerDestination toServerDestination(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufServerDestination.MinimalServerDestination.Builder builder = ProtobufServerDestination.MinimalServerDestination.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_DESTINATIONS:
                    String destinations = attribute.getValue();
                    if (destinations.contains(subsitutionValues.uidFromGeoChat)) {
                        destinations = destinations.replace(subsitutionValues.uidFromGeoChat, UID_SUBSTITUTION_MARKER);
                    }
                    builder.setDestinations(destinations);
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: serverdestination." + attribute.getName());
            }
        }
        return builder.build();
    }

    private ProtobufModel.MinimalModel toModel(CotDetail cotDetail) throws UnknownDetailFieldException {
        ProtobufModel.MinimalModel.Builder builder =  ProtobufModel.MinimalModel.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_NAME:
                    builder.setName(attribute.getValue());
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: model." + attribute.getName());
            }
        }
        return builder.build();
    }

    private ProtobufChatGroup.MinimalChatGroup toChatGroup(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufChatGroup.MinimalChatGroup.Builder builder = ProtobufChatGroup.MinimalChatGroup.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_ID:
                    String id = attribute.getValue();
                    if (id.equals(subsitutionValues.idFromChat)) {
                        id = ID_SUBSTITUTION_MARKER;
                    }
                    builder.setId(id);
                    break;
                default:
                    if (attribute.getName().startsWith(KEY_UID)) {
                        String uid = attribute.getValue();
                        if (uid.equals(subsitutionValues.uidFromGeoChat)) {
                            uid = UID_SUBSTITUTION_MARKER;
                        } else {
                            subsitutionValues.uidsFromChatGroup.add(uid);
                        }
                        builder.addUid(uid);
                        break;
                    }
                    throw new UnknownDetailFieldException("Don't know how to handle child attribute: chat.chatgrp." + attribute.getName());
            }
        }
        return builder.build();
    }

    private ProtobufHierarchy.MinimalHierarchy toHierarchy(CotDetail cotDetail, SubsitutionValues subsitutionValues) throws UnknownDetailFieldException {
        ProtobufHierarchy.MinimalHierarchy.Builder builder = ProtobufHierarchy.MinimalHierarchy.newBuilder();
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle child attribute: chat.hierarchy." + attribute.getName());
            }
        }

        List<CotDetail> children = cotDetail.getChildren();
        for (CotDetail child : children) {
            switch (child.getElementName()) {
                case KEY_GROUP:
                    builder.setGroup(toGroup(child, subsitutionValues));
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle child object: chat.hierarchy." + child.getElementName());
            }
        }

        return builder.build();
    }

    private void toColor(CotDetail cotDetail) throws UnknownDetailFieldException {
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_VALUE:
                case KEY_ARGB:
                    // Do nothing, we drop this field
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: color." + attribute.getName());
            }
        }
    }
    private void toLabelsOn(CotDetail cotDetail) throws UnknownDetailFieldException {
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_VALUE:
                    // Do nothing, we are packing this into bits
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: labels_on." + attribute.getName());
            }
        }
    }

    private void toHeightUnit(CotDetail cotDetail) throws UnknownDetailFieldException {
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: height_unit." + attribute.getName());
            }
        }
    }

    private void toHeight(CotDetail cotDetail) throws UnknownDetailFieldException {
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_UNIT:
                case KEY_VALUE:
                    // Do nothing, we are packing these into bits
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: height." + attribute.getName());
            }
        }
    }

    private void toArchive(CotDetail cotDetail) throws UnknownDetailFieldException {
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: archive." + attribute.getName());
            }
        }
    }

    private void toStatus(CotDetail cotDetail) throws UnknownDetailFieldException {
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_READINESS:
                case KEY_BATTERY:
                    // Do nothing, we pack these fields into bits
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: status." + attribute.getName());
            }
        }
    }

    private void toUid(CotDetail cotDetail) throws UnknownDetailFieldException {
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_DROID:
                    // Do nothing, we drop this field
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: uid." + attribute.getName());
            }
        }
    }

    private void toPrecisionLocation(CotDetail cotDetail) throws UnknownDetailFieldException {
        CotAttribute[] attributes = cotDetail.getAttributes();
        for (CotAttribute attribute : attributes) {
            switch (attribute.getName()) {
                case KEY_ALTSRC:
                case KEY_GEOPOINTSRC:
                    // Do nothing, we pack these fields into bits
                    break;
                default:
                    throw new UnknownDetailFieldException("Don't know how to handle detail field: precisionLocation." + attribute.getName());
            }
        }
    }

    /**
     * CustomBytes Format (fixed64):
     *
     * time   (25 bits, seconds since start of year)
     * stale  (17 bits, seconds after `time`)
     * hae    (14 bits, whole meters)
     *
     * CustomBytesExt Format (fixed64):
     * how              (3 bits, mapping)
     * geopointsrc      (6 bits, mapping, high bit marks whether we have a value)
     * altsrc           (6 bits, mapping, high bit marks whether we have a value)
     * group.role       (5 bits, mapping, high bit marks whether we have a value)
     * status.battery   (8 bits, int, high bit marks whether we have a value)
     * status.readiness (2 bits, bool, high bit marks whether we have a value)
     * labels_on.value  (2 bits, bool, high bit marks whether we have a value)
     * height.unit      (4 bits, int, high bit marks whether we have a value)
     * height.value     (14 bits, int, high bit marks whether we have a value)
     */

    private static final int CUSTOM_FIELD_TIME_LENGTH = 25;
    private static final int CUSTOM_FIELD_STALE_LENGTH = 17;
    private static final int CUSTOM_FIELD_HAE_LENGTH = 14;

    private static final int CUSTOM_FIELD_HOW_LENGTH = 3;
    private static final int CUSTOM_FIELD_GEOPOINTSRC_LENGTH = 6;
    private static final int CUSTOM_FIELD_ALTSRC_LENGTH = 6;
    private static final int CUSTOM_FIELD_ROLE_LENGTH = 5;
    private static final int CUSTOM_FIELD_BATTERY_LENGTH = 8;
    private static final int CUSTOM_FIELD_READINESS_LENGTH = 2;
    private static final int CUSTOM_FIELD_LABELS_ON_LENGTH = 2;
    private static final int CUSTOM_FIELD_HEIGHT_UNIT_LENGTH = 4;
    private static final int CUSTOM_FIELD_HEIGHT_VALUE_LENGTH = 14;

    private long packCustomBytes(CoordinatedTime time, CoordinatedTime stale, double hae) throws MappingNotFoundException {
        ShiftTracker shiftTracker = new ShiftTracker();
        long customBytes = 0;

        long timeSinceStartOfYear = (time.getMilliseconds() - mStartOfYearMs) / 1000L;
        customBytes = packBits(customBytes, LONG_INT_LENGTH, timeSinceStartOfYear, CUSTOM_FIELD_TIME_LENGTH, shiftTracker);

        long timeUntilStale = (stale.getMilliseconds() - time.getMilliseconds()) / 1000L;
        customBytes = packBits(customBytes, LONG_INT_LENGTH, timeUntilStale, CUSTOM_FIELD_STALE_LENGTH, shiftTracker);

        customBytes = packBits(customBytes, LONG_INT_LENGTH, (long)hae, CUSTOM_FIELD_HAE_LENGTH, shiftTracker);

        return customBytes;
    }

    private long packCustomBytesExt(String how, String geoPointSrc, String altSrc, String groupRole, Integer battery, Boolean readiness, Boolean labelsOn, Integer heightUnit, Integer heightValue) throws MappingNotFoundException {
        ShiftTracker shiftTracker = new ShiftTracker();
        long customBytes = 0;

        customBytes = packNonNullMappedString(customBytes, LONG_INT_LENGTH, how, "how", CUSTOM_FIELD_HOW_LENGTH, MAPPING_HOW, shiftTracker);
        customBytes = packNullableMappedString(customBytes, LONG_INT_LENGTH, geoPointSrc, "geopointsrc", CUSTOM_FIELD_GEOPOINTSRC_LENGTH, MAPPING_ALTSRC_AND_GEOPOINTSRC, shiftTracker);
        customBytes = packNullableMappedString(customBytes, LONG_INT_LENGTH, altSrc, "altsrc", CUSTOM_FIELD_ALTSRC_LENGTH, MAPPING_ALTSRC_AND_GEOPOINTSRC, shiftTracker);
        customBytes = packNullableMappedString(customBytes, LONG_INT_LENGTH, groupRole, "role", CUSTOM_FIELD_ROLE_LENGTH, MAPPING_GROUP_ROLE, shiftTracker);
        customBytes = packNullableInt(customBytes, LONG_INT_LENGTH, battery, CUSTOM_FIELD_BATTERY_LENGTH, shiftTracker);
        customBytes = packNullableBoolean(customBytes, LONG_INT_LENGTH, readiness, CUSTOM_FIELD_READINESS_LENGTH, shiftTracker);
        customBytes = packNullableBoolean(customBytes, LONG_INT_LENGTH, labelsOn, CUSTOM_FIELD_LABELS_ON_LENGTH, shiftTracker);
        customBytes = packNullableInt(customBytes, LONG_INT_LENGTH, heightUnit, CUSTOM_FIELD_HEIGHT_UNIT_LENGTH, shiftTracker);
        customBytes = packNullableInt(customBytes, LONG_INT_LENGTH, heightValue, CUSTOM_FIELD_HEIGHT_VALUE_LENGTH, shiftTracker);

        return customBytes;
    }

    /**
     * toCotEvent
     */

    private CotDetail cotDetailFromProtoDetail(ProtobufDetail.MinimalDetail detail, CotEvent cotEvent, CustomBytesExtFields customBytesExtFields, SubsitutionValues subsitutionValues) {
        CotDetail cotDetail = new CotDetail();

        ProtobufTakv.MinimalTakv takv = detail.getTakv();
        if (takv != null && takv != ProtobufTakv.MinimalTakv.getDefaultInstance()) {
            CotDetail takvDetail = new CotDetail(KEY_TAKV);

            takvDetail.setAttribute(KEY_OS, Integer.toString(takv.getOs()));
            if (!isNullOrEmpty(takv.getVersion())) {
                takvDetail.setAttribute(KEY_VERSION, takv.getVersion());
            }
            if (!isNullOrEmpty(takv.getDevice())) {
                takvDetail.setAttribute(KEY_DEVICE, takv.getDevice());
            }
            if (!isNullOrEmpty(takv.getPlatform())) {
                takvDetail.setAttribute(KEY_PLATFORM, takv.getPlatform());
            }
            cotDetail.addChild(takvDetail);
        }

        ProtobufContact.MinimalContact contact = detail.getContact();
        if (contact != null && contact != ProtobufContact.MinimalContact.getDefaultInstance()) {
            cotDetail.addChild(contactFromProtoContact(contact, subsitutionValues, cotEvent.getType().equals(CotMessageTypes.TYPE_PLI)));
        }

        if (!isNullOrEmpty(customBytesExtFields.altSrc) || !isNullOrEmpty(customBytesExtFields.geoPointSrc)) {
            CotDetail precisionLocationDetail = new CotDetail(KEY_PRECISIONLOCATION);

            if (!isNullOrEmpty(customBytesExtFields.altSrc)) {
                precisionLocationDetail.setAttribute(KEY_ALTSRC, customBytesExtFields.altSrc);
            }
            if (!isNullOrEmpty(customBytesExtFields.geoPointSrc)) {
                precisionLocationDetail.setAttribute(KEY_GEOPOINTSRC, customBytesExtFields.geoPointSrc);
            }

            cotDetail.addChild(precisionLocationDetail);
        }

        ProtobufUnderscoreGroup.MinimalUnderscoreGroup group = detail.getGroup();
        if (group != null && group != ProtobufUnderscoreGroup.MinimalUnderscoreGroup.getDefaultInstance()) {
            CotDetail groupDetail = new CotDetail(KEY_UNDERSCORED_GROUP);

            if (!isNullOrEmpty(group.getName())) {
                groupDetail.setAttribute(KEY_NAME, group.getName());
            }
            if (!isNullOrEmpty(customBytesExtFields.role)) {
                groupDetail.setAttribute(KEY_ROLE, customBytesExtFields.role);
            }

            cotDetail.addChild(groupDetail);
        }

        if (customBytesExtFields.battery != null || customBytesExtFields.readiness != null) {
            CotDetail statusDetail = new CotDetail(KEY_STATUS);

            if (customBytesExtFields.battery != null) {
                statusDetail.setAttribute(KEY_BATTERY, Integer.toString(customBytesExtFields.battery));
            }
            if (customBytesExtFields.readiness != null) {
                statusDetail.setAttribute(KEY_READINESS, Boolean.toString(customBytesExtFields.readiness));
            }

            cotDetail.addChild(statusDetail);
        }

        ProtobufTrack.MinimalTrack track = detail.getTrack();
        if (track != null && track != ProtobufTrack.MinimalTrack.getDefaultInstance()) {
            CotDetail trackDetail = new CotDetail(KEY_TRACK);

            if (track.getCourse() != 0.0) {
                trackDetail.setAttribute(KEY_COURSE, Double.toString(track.getCourse()));
            }
            if (track.getSpeed() != 0.0) { // TODO: better handling of nulls
                trackDetail.setAttribute(KEY_SPEED, Double.toString(track.getSpeed()));
            }
            cotDetail.addChild(trackDetail);
        }

        ProtobufRemarks.MinimalRemarks remarks = detail.getRemarks();
        if (remarks != null && remarks != ProtobufRemarks.MinimalRemarks.getDefaultInstance()) {
            CotDetail remarksDetail = new CotDetail(KEY_REMARKS);

            if (!isNullOrEmpty(remarks.getRemarks())) {
                remarksDetail.setInnerText(remarks.getRemarks());
            }
            String source = remarks.getSource();
            if (!isNullOrEmpty(source)) {
                if (source.contains(UID_SUBSTITUTION_MARKER)) {
                    source = source.replace(UID_SUBSTITUTION_MARKER, subsitutionValues.uidFromGeoChat);
                }
                remarksDetail.setAttribute(KEY_SOURCE, source);
            }
            if (!isNullOrEmpty(remarks.getTo())) {
                remarksDetail.setAttribute(KEY_TO, remarks.getTo());
            }
            long timeOffset = remarks.getTime();
            if (timeOffset > 0) {
                long timeMs = mStartOfYearMs + (timeOffset * 1000);
                CoordinatedTime time = new CoordinatedTime(timeMs);
                remarksDetail.setAttribute(KEY_TIME, time.toString());
            }

            cotDetail.addChild(remarksDetail);
        }

        ProtobufLink.MinimalLink link = detail.getLink();
        if (link != null && link != ProtobufLink.MinimalLink.getDefaultInstance()) {
            CotDetail linkDetail = new CotDetail(KEY_LINK);

            String uid = link.getUid();
            if (!isNullOrEmpty(uid)) {
                if (uid.equals(UID_SUBSTITUTION_MARKER)) {
                    uid = subsitutionValues.uidFromGeoChat;
                }
                linkDetail.setAttribute(KEY_UID, uid);
            }
            if (!isNullOrEmpty(link.getType())) {
                linkDetail.setAttribute(KEY_TYPE, link.getType());
            }
            if (!isNullOrEmpty(link.getParentCallsign())) {
                linkDetail.setAttribute(KEY_PARENT_CALLSIGN, link.getParentCallsign());
            }
            if (!isNullOrEmpty(link.getRelation())) {
                linkDetail.setAttribute(KEY_RELATION, link.getRelation());
            }
            long productionTimeOffset = link.getProductionTime();
            if (productionTimeOffset > 0) {
                long productionTimeMs = mStartOfYearMs + (productionTimeOffset * 1000);
                CoordinatedTime productionTime = new CoordinatedTime(productionTimeMs);
                linkDetail.setAttribute(KEY_PRODUCTION_TIME, productionTime.toString());
            }

            cotDetail.addChild(linkDetail);
        }

        ProtobufChat.MinimalChat chat = detail.getChat();
        if (chat != null && chat != ProtobufChat.MinimalChat.getDefaultInstance()) {
            CotDetail chatDetail = new CotDetail(KEY_CHAT);

            String parent = chat.getParent();
            if (!isNullOrEmpty(parent)) {
                if (parent.equals(USER_GROUPS_SUBSTITUTION_MARKER)) {
                    parent = VALUE_USER_GROUPS;
                }
                chatDetail.setAttribute(KEY_PARENT, parent);
            }
            if (chat.getGroupOwner() > 0) {
                chatDetail.setAttribute(KEY_GROUP_OWNER, Boolean.toString((chat.getGroupOwner() & createBitMask(1)) == 1));
            }
            String chatroom = chat.getChatroom();
            if (!isNullOrEmpty(chatroom)) {
                if (chatroom.equals(CHATROOM_SUBSTITUTION_MARKER)) {
                    chatroom = subsitutionValues.chatroomFromGeoChat;
                }
                chatDetail.setAttribute(KEY_CHATROOM, chatroom);
            }
            subsitutionValues.idFromChat = chat.getId();
            if (!isNullOrEmpty(subsitutionValues.idFromChat)) {
                chatDetail.setAttribute(KEY_ID, subsitutionValues.idFromChat);
            }
            subsitutionValues.senderCallsignFromChat = chat.getSenderCallsign();
            if (!isNullOrEmpty(subsitutionValues.senderCallsignFromChat)) {
                chatDetail.setAttribute(KEY_SENDER_CALLSIGN, subsitutionValues.senderCallsignFromChat);
            }

            ProtobufChatGroup.MinimalChatGroup chatGroup = chat.getChatGroup();
            if (chatGroup != null && chatGroup != ProtobufChatGroup.MinimalChatGroup.getDefaultInstance()) {
                CotDetail chatGroupDetail = new CotDetail(KEY_CHAT_GROUP);

                String id = chatGroup.getId();
                if (!isNullOrEmpty(id)) {
                    if (id.equals(ID_SUBSTITUTION_MARKER)) {
                        id = subsitutionValues.idFromChat;
                    }
                    chatGroupDetail.setAttribute(KEY_ID, id);
                }
                List<String> uidList = chatGroup.getUidList();
                for (int i = 0 ; i < uidList.size() ; i++) {
                    String uid = uidList.get(i);
                    if (uid.equals(UID_SUBSTITUTION_MARKER)) {
                        uid = subsitutionValues.uidFromGeoChat;
                    } else {
                        subsitutionValues.uidsFromChatGroup.add(uid);
                    }
                    chatGroupDetail.setAttribute(KEY_UID + i, uid);
                }

                chatDetail.addChild(chatGroupDetail);
            }

            ProtobufHierarchy.MinimalHierarchy hierarchy = chat.getHierarchy();
            if (hierarchy != null && hierarchy != ProtobufHierarchy.MinimalHierarchy.getDefaultInstance()) {
                CotDetail hierarchyDetail = new CotDetail(KEY_HIERARCHY);

                ProtobufGroup.MinimalGroup hierarchyGroup = hierarchy.getGroup();
                if (hierarchyGroup != null && hierarchyGroup != ProtobufGroup.MinimalGroup.getDefaultInstance()) {
                    hierarchyDetail.addChild(groupFromProtoGroup(hierarchyGroup, subsitutionValues));
                }

                chatDetail.addChild(hierarchyDetail);
            }

            cotDetail.addChild(chatDetail);
        }

        ProtobufServerDestination.MinimalServerDestination serverDestination = detail.getServerDestination();
        if (serverDestination != null && serverDestination != ProtobufServerDestination.MinimalServerDestination.getDefaultInstance()) {
            CotDetail serverDestinationDetail = new CotDetail(KEY_SERVER_DESTINATION);

            String destinations = serverDestination.getDestinations();
            if (!isNullOrEmpty(destinations)) {
                if (destinations.contains(UID_SUBSTITUTION_MARKER)) {
                    destinations = destinations.replace(UID_SUBSTITUTION_MARKER, subsitutionValues.uidFromGeoChat);
                }
                serverDestinationDetail.setAttribute(KEY_DESTINATIONS, destinations);
            }

            cotDetail.addChild(serverDestinationDetail);
        }

        ProtobufModel.MinimalModel model = detail.getModel();
        if (model != null && model != ProtobufModel.MinimalModel.getDefaultInstance()) {
            CotDetail modelDetail = new CotDetail(KEY_MODEL);

            if (!isNullOrEmpty(model.getName())) {
                modelDetail.setAttribute(KEY_NAME, model.getName());
            }

            cotDetail.addChild(modelDetail);
        }

        if (customBytesExtFields.labelsOn != null) {
            CotDetail labelsOnDetail = new CotDetail(KEY_LABELS_ON);

            labelsOnDetail.setAttribute(KEY_VALUE, Boolean.toString(customBytesExtFields.labelsOn));

            cotDetail.addChild(labelsOnDetail);
        }

        if (customBytesExtFields.heightUnit != null || customBytesExtFields.heightValue != null) {
            CotDetail heightDetail = new CotDetail(KEY_HEIGHT);

            if (customBytesExtFields.heightUnit != null) {
                CotDetail heightUnitDetail = new CotDetail(KEY_HEIGHT_UNIT);

                heightUnitDetail.setInnerText(Integer.toString(customBytesExtFields.heightUnit));

                cotDetail.addChild(heightUnitDetail);

                heightDetail.setAttribute(KEY_UNIT, MAPPING_HEIGHT_UNIT[customBytesExtFields.heightUnit]);
            }

            if (customBytesExtFields.heightValue != null) {
                String heightValueStr = Float.toString(customBytesExtFields.heightValue);
                heightDetail.setInnerText(heightValueStr);
                heightDetail.setAttribute(KEY_VALUE, heightValueStr);
            }

            cotDetail.addChild(heightDetail);
        }

        String iconSetPath = detail.getIconSetPath();
        if (!isNullOrEmpty(iconSetPath)) {
            CotDetail userIconDetail = new CotDetail(KEY_USER_ICON);

            userIconDetail.setAttribute(KEY_ICON_SET_PATH, iconSetPath);

            cotDetail.addChild(userIconDetail);
        }

        return cotDetail;
    }

    private CotDetail groupFromProtoGroup(ProtobufGroup.MinimalGroup group, SubsitutionValues subsitutionValues) {
        CotDetail cotDetail = new CotDetail(KEY_GROUP);

        String name = group.getName();
        if (!isNullOrEmpty(name)) {
            if (name.equals(CHATROOM_SUBSTITUTION_MARKER)) {
                name = subsitutionValues.chatroomFromGeoChat;
            } else if (name.equals(GROUPS_SUBSTITUION_MARKER)) {
                name = VALUE_GROUPS;
            }
            cotDetail.setAttribute(KEY_NAME, name);
        }

        String uid = group.getUid();
        if (!isNullOrEmpty(uid)) {
            if (uid.equals(ID_SUBSTITUTION_MARKER)) {
                uid = subsitutionValues.idFromChat;
            } else if (uid.equals(USER_GROUPS_SUBSTITUTION_MARKER)) {
                uid = VALUE_USER_GROUPS;
            }
            cotDetail.setAttribute(KEY_UID, uid);
        }

        List<ProtobufContact.MinimalContact> contacts = group.getContactList();
        for (ProtobufContact.MinimalContact contact : contacts) {
            cotDetail.addChild(contactFromProtoContact(contact, subsitutionValues, false));
        }

        ProtobufGroup.MinimalGroup nestedGroup = group.getGroup();
        if (nestedGroup != null && nestedGroup != ProtobufGroup.MinimalGroup.getDefaultInstance()) {
            cotDetail.addChild(groupFromProtoGroup(nestedGroup, subsitutionValues));
        }

        return cotDetail;
    }

    private CotDetail contactFromProtoContact(ProtobufContact.MinimalContact contact, SubsitutionValues subsitutionValues, boolean isPli) {
        CotDetail contactDetail = new CotDetail(KEY_CONTACT);

        String name = contact.getName();
        if (!isNullOrEmpty(name)) {
            if (name.equals(SENDER_CALLSIGN_SUBSTITUION_MARKER)) {
                name = subsitutionValues.senderCallsignFromChat;
            }
            contactDetail.setAttribute(KEY_NAME, name);
        }

        if (!isNullOrEmpty(contact.getCallsign())) {
            contactDetail.setAttribute(KEY_CALLSIGN, contact.getCallsign());
        }

        String uid = contact.getUid();
        if (!isNullOrEmpty(uid)) {
            if (uid.equals(UID_SUBSTITUTION_MARKER)) {
                uid = subsitutionValues.uidFromGeoChat;
            } else if (uid.startsWith(UID_SUBSTITUTION_MARKER)) {
                int index = Integer.parseInt(uid.replace(UID_SUBSTITUTION_MARKER, ""));
                uid = subsitutionValues.uidsFromChatGroup.get(index);
            }
            contactDetail.setAttribute(KEY_UID, uid);
        }

        if (contact.getEndpointAddr() != 0) {
            try {
                byte[] endpointAddrAsBytes = BigInteger.valueOf(contact.getEndpointAddr()).toByteArray();
                String ipAddress = InetAddress.getByAddress(endpointAddrAsBytes).getHostAddress();
                contactDetail.setAttribute(KEY_ENDPOINT, ipAddress + DEFAULT_CHAT_PORT_AND_PROTO);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else if (isPli) {
            // PLI from a device that doesn't have an IP address, fake it
            contactDetail.setAttribute(KEY_ENDPOINT, FAKE_ENDPOINT_ADDRESS);
        }

        return contactDetail;
    }

    private CustomBytesFields unpackCustomBytes(long customBytes) {
        ShiftTracker shiftTracker = new ShiftTracker();

        long timeSinceStartOfYear = unpackBits(customBytes, LONG_INT_LENGTH, CUSTOM_FIELD_TIME_LENGTH, shiftTracker);
        CoordinatedTime time = new CoordinatedTime(mStartOfYearMs + timeSinceStartOfYear * 1000);

        long timeUntilStale = unpackBits(customBytes, LONG_INT_LENGTH, CUSTOM_FIELD_STALE_LENGTH, shiftTracker);
        CoordinatedTime stale = new CoordinatedTime(time.getMilliseconds() + timeUntilStale * 1000);

        double hae = (double)unpackBits(customBytes, LONG_INT_LENGTH, CUSTOM_FIELD_HAE_LENGTH, shiftTracker);

        return new CustomBytesFields(time, stale, hae);
    }

    private CustomBytesExtFields unpackCustomBytesExt(long customBytesExt) {
        ShiftTracker shiftTracker = new ShiftTracker();

        String how = unpackNonNullMappedString(customBytesExt, LONG_INT_LENGTH, CUSTOM_FIELD_HOW_LENGTH, MAPPING_HOW, shiftTracker);
        String geoPointSrc = unpackNullableMappedString(customBytesExt, LONG_INT_LENGTH, CUSTOM_FIELD_GEOPOINTSRC_LENGTH, MAPPING_ALTSRC_AND_GEOPOINTSRC, shiftTracker);
        String altSrc = unpackNullableMappedString(customBytesExt, LONG_INT_LENGTH, CUSTOM_FIELD_ALTSRC_LENGTH, MAPPING_ALTSRC_AND_GEOPOINTSRC, shiftTracker);
        String role = unpackNullableMappedString(customBytesExt, LONG_INT_LENGTH, CUSTOM_FIELD_ROLE_LENGTH, MAPPING_GROUP_ROLE, shiftTracker);
        Integer battery = unpackNullableInt(customBytesExt, LONG_INT_LENGTH, CUSTOM_FIELD_BATTERY_LENGTH, shiftTracker);
        Boolean readiness = unpackNullableBoolean(customBytesExt, LONG_INT_LENGTH, CUSTOM_FIELD_READINESS_LENGTH, shiftTracker);
        Boolean labelsOn = unpackNullableBoolean(customBytesExt, LONG_INT_LENGTH, CUSTOM_FIELD_LABELS_ON_LENGTH, shiftTracker);
        Integer heightUnit = unpackNullableInt(customBytesExt, LONG_INT_LENGTH, CUSTOM_FIELD_HEIGHT_UNIT_LENGTH, shiftTracker);
        Integer heightValue = unpackNullableInt(customBytesExt, LONG_INT_LENGTH, CUSTOM_FIELD_HEIGHT_VALUE_LENGTH, shiftTracker);

        return new CustomBytesExtFields(how, geoPointSrc, altSrc, role, battery, readiness, labelsOn, heightUnit, heightValue);
    }

    /**
     * Utils
     */
    private int findMappingForArray(String fieldName, Object[] array, Object objectToFind) throws MappingNotFoundException {
        int index = ArrayUtils.indexOf(array, objectToFind);

        if (index == -1) {
            throw new MappingNotFoundException("Could not find mapping for field: " + fieldName + ", value: " + objectToFind);
        }
        return index;
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private long setFieldNull(long customBytes, int containerLength, int fieldLength, ShiftTracker shiftTracker) {
        customBytes |= 1L << containerLength - shiftTracker.accumulatedShift - 1;
        shiftTracker.accumulatedShift += fieldLength;
        return customBytes;
    }

    private long packNonNullMappedString(long customBytes, int containerLength, String value, String fieldName, int fieldLength, String[] mapping, ShiftTracker shiftTracker) throws MappingNotFoundException {
        int index = findMappingForArray(fieldName, mapping, value);
        customBytes |= (index & createBitMask(fieldLength)) << containerLength - shiftTracker.accumulatedShift - fieldLength;
        shiftTracker.accumulatedShift += fieldLength;
        return customBytes;
    }

    private long packNullableMappedString(long customBytes, int containerLength, String value, String fieldName, int fieldLength, String[] mapping, ShiftTracker shiftTracker) throws MappingNotFoundException {
        if (value != null) {
            return packNonNullMappedString(customBytes, containerLength, value, fieldName, fieldLength, mapping, shiftTracker);
        }
        return setFieldNull(customBytes, containerLength, fieldLength, shiftTracker);
    }

    private long packNullableInt(long customBytes, int containerLength, Integer value, int fieldLength, ShiftTracker shiftTracker) {
        if (value != null) {
            customBytes |= (value & createBitMask(fieldLength)) << containerLength - shiftTracker.accumulatedShift - fieldLength;
            shiftTracker.accumulatedShift += fieldLength;
            return customBytes;
        }
        return setFieldNull(customBytes, containerLength, fieldLength, shiftTracker);
    }

    private long packNullableBoolean(long customBytes, int containerLength, Boolean value, int fieldLength, ShiftTracker shiftTracker) {
        if (value != null) {
            customBytes |= ((value ? 1 : 0)) & createBitMask(1) << containerLength - shiftTracker.accumulatedShift - fieldLength;
            shiftTracker.accumulatedShift += fieldLength;
            return customBytes;
        }
        return setFieldNull(customBytes, containerLength, fieldLength, shiftTracker);
    }

    private long packBits(long customBytes, int containerLength, long value, int fieldLength, ShiftTracker shiftTracker) {
        customBytes |= (value & createBitMask(fieldLength)) << containerLength - shiftTracker.accumulatedShift - fieldLength;
        shiftTracker.accumulatedShift += fieldLength;
        return customBytes;
    }

    private boolean hasNullableField(long customBytes, int containerLength, ShiftTracker shiftTracker) {
        return (customBytes >>> containerLength - shiftTracker.accumulatedShift - 1 & createBitMask(1)) == 0;
    }

    @Nullable
    private String unpackNullableMappedString(long customBytes, int containerLength, int fieldLength, String[] mapping, ShiftTracker shiftTracker) {
        if (!hasNullableField(customBytes, containerLength, shiftTracker)) {
            shiftTracker.accumulatedShift += fieldLength;
            return null;
        }
        return unpackNonNullMappedString(customBytes, containerLength, fieldLength, mapping, shiftTracker);
    }

    @NonNull
    private String unpackNonNullMappedString(long customBytes, int containerLength, int fieldLength, String[] mapping, ShiftTracker shiftTracker) {
        int index = (int)(customBytes >>> containerLength - shiftTracker.accumulatedShift - fieldLength & createBitMask(fieldLength));
        shiftTracker.accumulatedShift += fieldLength;
        return mapping[index];
    }

    @Nullable
    private Integer unpackNullableInt(long customBytes, int containerLength, int fieldLength, ShiftTracker shiftTracker) {
        Integer value = null;
        if (hasNullableField(customBytes, containerLength, shiftTracker)) {
            value = (int)(customBytes >>> containerLength - shiftTracker.accumulatedShift - fieldLength & createBitMask(fieldLength));
        }
        shiftTracker.accumulatedShift += fieldLength;
        return value;
    }

    @Nullable
    private Boolean unpackNullableBoolean(long customBytes, int containerLength, int fieldLength, ShiftTracker shiftTracker) {
        Boolean value = null;
        if (hasNullableField(customBytes, containerLength, shiftTracker)) {
            value = (customBytes >>> containerLength - shiftTracker.accumulatedShift - fieldLength & createBitMask(fieldLength)) == 1;
        }
        shiftTracker.accumulatedShift += fieldLength;
        return value;
    }

    private long unpackBits(long customBytes, int containerLength, int fieldLength, ShiftTracker shiftTracker) {
        long value = customBytes >>> containerLength - shiftTracker.accumulatedShift - fieldLength & createBitMask(fieldLength);
        shiftTracker.accumulatedShift += fieldLength;
        return value;
    }

    private long createBitMask(int bits) {
        long bitmask = bits > 0 ? 1 : 0;
        for (int i = 1 ; i < bits ; i++) {
            bitmask |= 1 << i;
        }
        return bitmask;
    }

    /**
     * Data Classes
     */
    private static class CustomBytesFields {
        public final CoordinatedTime time;
        public final CoordinatedTime stale;
        public final double hae;

        public CustomBytesFields(CoordinatedTime time, CoordinatedTime stale, double hae) {
            this.time = time;
            this.stale = stale;
            this.hae = hae;
        }
    }

    private static class CustomBytesExtFields {
        public final String how;
        public final String geoPointSrc;
        public final String altSrc;
        public final String role;
        public final Integer battery;
        public final Boolean readiness;
        public final Boolean labelsOn;
        public final Integer heightUnit;
        public final Integer heightValue;

        public CustomBytesExtFields(String how, String geoPointSrc, String altSrc, String role, Integer battery, Boolean readiness, Boolean labelsOn, Integer heightUnit, Integer heightValue) {
            this.how = how;
            this.geoPointSrc = geoPointSrc;
            this.altSrc = altSrc;
            this.role = role;
            this.battery = battery;
            this.readiness = readiness;
            this.labelsOn = labelsOn;
            this.heightUnit = heightUnit;
            this.heightValue = heightValue;
        }
    }

    private static class SubsitutionValues {
        public String uidFromGeoChat;
        public String chatroomFromGeoChat;
        public String idFromChat;
        public String senderCallsignFromChat;
        public List<String> uidsFromChatGroup = new ArrayList<>();
    }

    private static class ShiftTracker {
        public int accumulatedShift = 0;
    }

    public static class UnknownDetailFieldException extends Exception {
        public UnknownDetailFieldException(String message) {
            super(message);
        }
    }

    public static class MappingNotFoundException extends Exception {
        public MappingNotFoundException(String message) {
            super(message);
        }
    }
}