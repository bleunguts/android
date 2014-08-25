package org.syncloud.discovery;

import org.syncloud.ssh.model.DirectEndpoint;

public interface DeviceEndpointListener {
    public void added(DirectEndpoint endpoint);
    public void removed(DirectEndpoint endpoint);
}
