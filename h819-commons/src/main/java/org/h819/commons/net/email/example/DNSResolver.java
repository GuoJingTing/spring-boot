/*
 * Copyright 2003 The Apache Software Foundation.
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


package org.h819.commons.net.email.example;

import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Utility class to resolve names against DN servers
 */
@Slf4j
public class DNSResolver {

    //private static final Logger log = LoggerFactory.getLogger(DNSResolver.class);

    /**
     * check DNS.
     *
     * @param hostname hostname
     * @return true if valid
     */
    public static boolean checkDNS(String hostname) {
        return checkDNS(hostname, false);
    }

    /**
     * check DNS.
     *
     * @param hostname hostname
     * @param mx       do MX query or not
     * @return true if valid
     */
    public static boolean checkDNS(String hostname, boolean mx) {
        List<String> records = resolveDNS(hostname, mx);

        for(String s : records)
        System.out.println(s);

        return records != null && records.size() > 0;
    }

    /**
     * Resolve MX DNS.
     *
     * @param hostname hostname
     * @return list of MXs
     */
    public static List<String> resolveDNS(String hostname, boolean mx) {
        List<String> result = new ArrayList<String>();

        try {
            log.trace("DNS validation: resolving DNS for " + hostname + " " + (mx ? "(MX)" : "(A/CNAME)"));

            Hashtable env = new Hashtable();

            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "5000");    /* quite short... too short? */
            env.put("com.sun.jndi.dns.timeout.retries", "1");

            DirContext ictx = new InitialDirContext(env);
            String[] ids = (mx ? new String[]{"MX"} : new String[]{"A", "CNAME"});
            Attributes attrs = ictx.getAttributes(hostname, ids);

            if (mx) {
                Attribute attr = attrs.get("MX");

                if (attr != null && attr.size() > 0) {
                    NamingEnumeration e = attr.getAll();

                    while (e.hasMore()) {
                        String mxs = (String) e.next();
                        String f[] = mxs.split("\\s+");

                        for (int i = 0; i < f.length; i++) {
                            if (f[i].endsWith(".")) {
                                result.add(f[i].substring(0, f[i].length() - 1));
                            }
                        }
                    }
                    return result;
                } else {
                    log.trace("DNS validation: DNS query of '" + hostname + "' failed");
                    return null;
                }
            } else {
                Attribute attr = attrs.get("A");

                if (attr != null && attr.size() > 0) {
                    NamingEnumeration e = attr.getAll();

                    while (e.hasMore()) {
                        result.add((String) e.next());
                    }
                    return result;
                } else {
                    attr = attrs.get("CNAME");
                    if (attr != null && attr.size() > 0) {
                        NamingEnumeration e = attr.getAll();

                        while (e.hasMore()) {
                            String h = (String) e.next();

                            if (h.endsWith(".")) {
                                h = h.substring(0, h.lastIndexOf('.'));
                            }
                            log.trace("DNS validation: recursing on CNAME record towards host " + h);
                            result.addAll(resolveDNS(h, false));
                        }
                        return result;
                    } else {
                        log.trace("DNS validation: DNS query of '" + hostname + "' failed");
                        return null;
                    }
                }
            }
        } catch (NamingException ne) {
            log.trace("DNS validation: DNS MX query failed: " + ne.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
//        System.err.println("Outcome: "
//                + CheckEmailObj.checkEmail("172986681sff6@qq.com"));

        String qqhostName = "qq.com";
        String qqhostName2 = "outlook.com";

        System.out.println(DNSResolver.checkDNS(qqhostName2, false));
       // System.out.println(DNSResolver.checkDNS("outlook.com", true));


        try {
            Lookup  lookup = new Lookup(qqhostName2, Type.A);

            lookup.run();

            for(Record r : lookup.getAnswers())
            System.out.println(r);


        } catch (TextParseException e) {
            e.printStackTrace();
        }





        // System.out.println( MailChecker.validate("h819000sfasfsfsf@gmail.com"));
    }
}

