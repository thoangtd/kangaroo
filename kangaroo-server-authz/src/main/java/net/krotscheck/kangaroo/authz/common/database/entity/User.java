/*
 * Copyright (c) 2016 Michael Krotscheck
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.krotscheck.kangaroo.authz.common.database.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModelProperty;
import net.krotscheck.kangaroo.common.hibernate.id.AbstractEntityReferenceDeserializer;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * The user entity, as persisted to the database.
 *
 * @author Michael Krotscheck
 */
@Entity
@Table(name = "users")
@Indexed(index = "users")
@Analyzer(definition = "entity_analyzer")
public final class User extends AbstractAuthzEntity {

    /**
     * The Application to whom this user belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application", nullable = false, updatable = false)
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonDeserialize(using = Application.Deserializer.class)
    @IndexedEmbedded(includePaths = {"id", "owner.id"})
    @ApiModelProperty(
            required = true,
            dataType = "string",
            example = "3f631a2d6a04f5cc55f9e192f45649b7"
    )
    private Application application;

    /**
     * The Applications which this user owns (admin app only).
     */
    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "owner",
            cascade = {CascadeType.REMOVE},
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Application> applications = new ArrayList<>();

    /**
     * The user's role in this application.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role", nullable = true, updatable = true)
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonDeserialize(using = Role.Deserializer.class)
    @IndexedEmbedded(includePaths = {"id"})
    @ApiModelProperty(
            required = true,
            dataType = "string",
            example = "3f631a2d6a04f5cc55f9e192f45649b7"
    )
    private Role role;

    /**
     * List of this user's identities.
     */
    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "user",
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    @IndexedEmbedded(includePaths = {"claims", "remoteId"})
    private List<UserIdentity> identities = new ArrayList<>();

    /**
     * Get the application this user belongs to.
     *
     * @return The application.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Set the application which this user belongs.
     *
     * @param application The new application.
     */
    public void setApplication(final Application application) {
        this.application = application;
    }

    /**
     * Get the role for this user.
     *
     * @return The user's role.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Set the role for this user.
     *
     * @param role The user's new role.
     */
    public void setRole(final Role role) {
        this.role = role;
    }

    /**
     * Get the identities for this user.
     *
     * @return A list of identities.
     */
    public List<UserIdentity> getIdentities() {
        return identities;
    }

    /**
     * Set the value for this user's identities.
     *
     * @param identities The new list of identities.
     */
    public void setIdentities(final List<UserIdentity> identities) {
        this.identities = new ArrayList<>(identities);
    }

    /**
     * Get the list of owned applications.
     *
     * @return A list of owned applications.
     */
    public List<Application> getApplications() {
        return applications;
    }

    /**
     * Set the list of owned applications.
     *
     * @param applications A new list of applications.
     */
    public void setApplications(final List<Application> applications) {
        this.applications = new ArrayList<>(applications);
    }

    /**
     * The owner of this entity.
     *
     * @return This entity's owner, if it exists.
     */
    @Override
    @Transient
    @JsonIgnore
    public User getOwner() {
        if (application != null) {
            return application.getOwner();
        }
        return null;
    }

    /**
     * Deserialize a reference to an User.
     *
     * @author Michael Krotschecks
     */
    public static final class Deserializer
            extends AbstractEntityReferenceDeserializer<User> {

        /**
         * Constructor.
         */
        public Deserializer() {
            super(User.class);
        }
    }
}
