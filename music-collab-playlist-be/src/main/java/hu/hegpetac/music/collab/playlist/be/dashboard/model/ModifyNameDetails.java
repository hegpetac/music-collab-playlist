package hu.hegpetac.music.collab.playlist.be.dashboard.model;

import org.openapitools.model.ModifyNameResp;

public record ModifyNameDetails(
        String oldName,
        ModifyNameResp resp
) {}
