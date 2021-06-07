/*
 *  Copyright 2014 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lipisoft.toyshark;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lipisoft.toyshark.network.ip.IPv4Header;
import com.lipisoft.toyshark.transport.ITransportHeader;
import com.lipisoft.toyshark.transport.tcp.TCPHeader;
import com.lipisoft.toyshark.transport.udp.UDPHeader;

public class PacketInfoExtended {
    public final int protocol;
    public final String firstLine;
    public final int id;

    public PacketInfoExtended(int protocol, String firstLine, int id) {
        this.protocol = protocol;
        this.firstLine = firstLine;
        this.id = id;
    }


}
