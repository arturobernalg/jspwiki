/* 
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.  
 */
package org.apache.wiki.auth.acl;

import org.apache.wiki.api.core.AclEntry;
import org.apache.wiki.util.Synchronizer;

import java.io.Serializable;
import java.security.Permission;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;


/**
 * JSPWiki implementation of an Access Control List.
 *
 * @since 2.3
 */
public class AclImpl implements Acl, Serializable {

    /**
     * A lock used to ensure thread safety when accessing shared resources.
     * This lock provides more flexibility and capabilities than the intrinsic locking mechanism,
     * such as the ability to attempt to acquire a lock with a timeout, or to interrupt a thread
     * waiting to acquire a lock.
     *
     * @see java.util.concurrent.locks.ReentrantLock
     */
    private final ReentrantLock lock = new ReentrantLock();

    private static final long serialVersionUID = 1L;
    private final Vector< AclEntry > m_entries = new Vector<>();

    /** {@inheritDoc} */
    @Override
    public Principal[] findPrincipals( final Permission permission ) {
        final List< Principal > principals = new ArrayList<>();
        final Enumeration< AclEntry > entries = aclEntries();
        while( entries.hasMoreElements() ) {
            final AclEntry entry = entries.nextElement();
            final Enumeration< Permission > permissions = entry.permissions();
            while( permissions.hasMoreElements() ) {
                final Permission perm = permissions.nextElement();
                if( perm.implies( permission ) ) {
                    principals.add( entry.getPrincipal() );
                }
            }
        }
        return principals.toArray( new Principal[0] );
    }

    private boolean hasEntry( final AclEntry entry ) {
        if( entry == null ) {
            return false;
        }

        for( final AclEntry e : m_entries ) {
            final Principal ep = e.getPrincipal();
            final Principal entryp = entry.getPrincipal();

            if( ep == null || entryp == null ) {
                throw new IllegalArgumentException( "Entry is null; check code, please (entry=" + entry + "; e=" + e + ")" );
            }

            if( ep.getName().equals( entryp.getName() ) ) {
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addEntry( final AclEntry entry ) {
        return Synchronizer.synchronize(lock, () -> {
            if( entry.getPrincipal() == null ) {
                throw new IllegalArgumentException( "Entry principal cannot be null" );
            }

            if( hasEntry( entry ) ) {
                return false;
            }

            m_entries.add( entry );

            return true;
        });
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeEntry( final AclEntry entry ) {
        return Synchronizer.synchronize(lock, () -> m_entries.remove( entry ));
    }

    /** {@inheritDoc} */
    @Override
    public Enumeration< AclEntry > aclEntries() {
        return m_entries.elements();
    }

    /** {@inheritDoc} */
    @Override
    public AclEntry getAclEntry( final Principal principal ) {
        return m_entries.stream().filter(entry -> entry.getPrincipal().getName().equals(principal.getName())).findFirst().orElse(null);

    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return m_entries.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for( final AclEntry entry : m_entries ) {
            final Principal pal = entry.getPrincipal();
            if( pal != null ) {
                sb.append( "  user = " ).append( pal.getName() ).append( ": " );
            } else {
                sb.append( "  user = null: " );
            }
            sb.append( "(" );
            for( final Enumeration< Permission > perms = entry.permissions(); perms.hasMoreElements(); ) {
                final Permission perm = perms.nextElement();
                sb.append( perm.toString() );
            }
            sb.append( ")\n" );
        }
        return sb.toString();
    }

}
    
