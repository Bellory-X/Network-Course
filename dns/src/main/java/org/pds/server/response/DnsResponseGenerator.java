package org.pds.server.response;

import org.pds.server.request.DiscoverDnsRequest;
import org.pds.server.request.DnsRequest;
import org.pds.server.request.QueryDnsRequest;
import org.pds.server.request.RegisterDnsRequest;
import org.pds.util.DnsException;

import java.net.InetAddress;
import java.util.HashMap;

public class DnsResponseGenerator {

    private final InetAddress dnsServerAddress;
    private final HashMap<String, String> domainTable;

    public DnsResponseGenerator(
            InetAddress dnsServerAddress,
            HashMap<String, String> domainTable
    ) {
        this.dnsServerAddress = dnsServerAddress;
        this.domainTable = domainTable;
    }

    public DnsResponse getDnsResponse(DnsRequest request) {
        return switch (request) {
            case DiscoverDnsRequest ignore -> new DiscoverDnsResponse(dnsServerAddress);
            case QueryDnsRequest queryDnsRequest -> new QueryDnsResponse(domainTable.get(queryDnsRequest.domainName()));
            case RegisterDnsRequest registerDnsRequest -> {
                if (domainTable.containsKey(registerDnsRequest.domainName())) {
                    throw new DnsException("Duplicate domain name: " + registerDnsRequest.domainName());
                }
                domainTable.put(registerDnsRequest.domainName(), registerDnsRequest.address());
                yield new RegisterDnsResponse(registerDnsRequest.domainName(), registerDnsRequest.address());
            }
        };
    }
}
