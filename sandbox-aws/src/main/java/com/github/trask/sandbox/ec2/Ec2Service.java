/**
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.trask.sandbox.ec2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.ImportKeyPairRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyResult;
import com.amazonaws.services.identitymanagement.model.CreateUserRequest;
import com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.GetUserRequest;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest;
import com.amazonaws.services.identitymanagement.model.User;
import com.google.common.base.Objects;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

/**
 * @author Trask Stalnaker
 */
public class Ec2Service {

    public static final String AMAZON_LINUX_32BIT_AMI_ID = "ami-8c1fece5";
    public static final String AMAZON_LINUX_64BIT_AMI_ID = "ami-8e1fece7";
    public static final String UBUNTU_NATTY_32BIT_AMI_ID = "ami-06ad526f";
    public static final String UBUNTU_NATTY_64BIT_AMI_ID = "ami-1aad5273";

    public static final String T1_MICRO_INSTANCE_TYPE = "t1.micro";
    public static final String M1_SMALL_INSTANCE_TYPE = "m1.small";
    public static final String M1_LARGE_INSTANCE_TYPE = "m1.large";
    public static final String M1_XLARGE_INSTANCE_TYPE = "m1.xlarge";

    private static final Logger logger = LoggerFactory.getLogger(Ec2Service.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final AmazonEC2 ec2;
    private final AmazonIdentityManagement iam;

    public Ec2Service(AmazonEC2 ec2, AmazonIdentityManagement iam) {
        this.ec2 = ec2;
        this.iam = iam;
    }

    public Ec2Service(AWSCredentials credentials) {
        ec2 = new AmazonEC2Client(credentials);
        iam = new AmazonIdentityManagementClient(credentials);
    }

    public Ec2Service(File awsCredentialsFile) throws FileNotFoundException,
            IllegalArgumentException, IOException {

        AWSCredentials credentials = new PropertiesCredentials(awsCredentialsFile);
        ec2 = new AmazonEC2Client(credentials);
        iam = new AmazonIdentityManagementClient(credentials);
    }

    public void getOrCreateKeyPair(String keyName, String privateKeyPath)
            throws FileNotFoundException, JSchException, IOException {

        DescribeKeyPairsRequest request = new DescribeKeyPairsRequest().withKeyNames(keyName);
        try {
            ec2.describeKeyPairs(request);
        } catch (AmazonServiceException e) {
            createKeyPair(keyName, privateKeyPath);
        }
    }

    // this is unfortunately needed for the key pair used by jenkins
    // because jenkins looks for matching fingerprint using SHA-1
    // however aws fingerprints user uploaded keys using MD5
    // (see http://blog.jbrowne.com/?p=23)
    // (btw, the unfortunate part of this is that it unnecessarily
    // transfers the private key over the wire)
    public void getOrCreateKeyPairGenerateRemotely(String keyName, String privateKeyPath)
            throws IOException {

        DescribeKeyPairsRequest request = new DescribeKeyPairsRequest().withKeyNames(keyName);
        try {
            ec2.describeKeyPairs(request);
        } catch (AmazonServiceException e) {
            createKeyPairGenerateRemotely(keyName, privateKeyPath);
        }
    }

    @SuppressWarnings("serial")
    private static class PasswordNotSupportedException extends RuntimeException {}

    private void createKeyPair(String keyName, String privateKeyPath) throws FileNotFoundException,
            JSchException, IOException {

        if (!new File(privateKeyPath).exists()) {
            generateKey(privateKeyPath, keyName);
        }
        Reader r = new BufferedReader(new StringReader(FileUtils
                .readFileToString(new File(privateKeyPath))));
        PEMReader pem = new PEMReader(r, new PasswordFinder() {
            public char[] getPassword() {
                // this will get called if the private key is password protected
                // TODO deal with this here/elsewhere?
                throw new PasswordNotSupportedException();
            }
        });
        java.security.KeyPair pair = (java.security.KeyPair) pem.readObject();
        String publicKey = StringUtils.newStringIso8859_1(Base64
                .encodeBase64(pair.getPublic().getEncoded()));
        deleteKeyPairIfExists(keyName);
        ImportKeyPairRequest request = new ImportKeyPairRequest(keyName, publicKey);
        ec2.importKeyPair(request);
    }

    private void createKeyPairGenerateRemotely(String keyName, String privateKeyPath)
            throws IOException {

        deleteKeyPairIfExists(keyName);
        CreateKeyPairRequest request = new CreateKeyPairRequest(keyName);
        CreateKeyPairResult result = ec2.createKeyPair(request);
        FileUtils.writeStringToFile(new File(privateKeyPath),
                result.getKeyPair().getKeyMaterial());
    }

    private void deleteKeyPairIfExists(String keyName) {
        try {
            DeleteKeyPairRequest request = new DeleteKeyPairRequest(keyName);
            ec2.deleteKeyPair(request);
        } catch (AmazonServiceException e) {
            // ignore
        }
    }

    public void generateKey(String filename, String comment) throws JSchException,
            FileNotFoundException, IOException {

        // need to use jsch library for key generation (until sshj provides this?)
        JSch jsch = new JSch();
        KeyPair kpair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
        kpair.writePrivateKey(filename);
        kpair.writePublicKey(filename + ".pub", comment);
        kpair.dispose();
    }

    public Instance getOrCreateInstance(String name, String imageId, String instanceType,
            String securityGroupName, String keyPairName) throws IOException, InterruptedException {

        Instance instance = getInstanceForName(name);
        if (instance == null) {
            return createInstance(name, imageId, instanceType, securityGroupName, keyPairName);
        } else {
            String state = instance.getState().getName();
            if ("stopping".equals(state)) {
                // have to wait until stopped before the instance can be restarted
                for (int i = 0; i < 120 && "stopping".equals(state); i++) {
                    logger.info("main(): instance in the middle of stopping," +
                            " waiting to restart");
                    Thread.sleep(1000);
                    instance = getInstanceForName(name);
                    state = instance.getState().getName();
                }
            }
            if ("stopped".equals(state)) {
                return startInstance(instance.getInstanceId());
            }
            return instance;
        }
    }

    public Instance createInstance(String name, String imageId, String instanceType,
            String securityGroupName, String keyPairName) throws IOException, InterruptedException {

        logger.debug("newInstance(): name={}, imageId={}, instanceType={}, keyName={}",
                new String[] { name, imageId, instanceType, keyPairName });
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withImageId(imageId)
                .withMinCount(1)
                .withMaxCount(1)
                .withInstanceType(instanceType)
                .withKeyName(keyPairName)
                .withSecurityGroups(securityGroupName);
        logger.debug("newInstance(): calling ec2.runInstances()");
        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        List<Instance> instances = runInstancesResult.getReservation().getInstances();
        Instance instance = instances.get(0);
        CreateTagsRequest createTagsRequest = new CreateTagsRequest()
                .withResources(instance.getInstanceId())
                .withTags(new Tag("Name", name));
        System.out.println(instance.getState().getName());
        try {
            // occasionally the create tag request reports that the instance id does not exist
            // so we retry a couple of times
            logger.debug("newInstance(): calling ec2.createTags(\"Name\", {})", name);
            ec2.createTags(createTagsRequest);
        } catch (AmazonServiceException e) {
            Thread.sleep(1000);
            logger.debug("newInstance(): 2nd try ... calling ec2.createTags(\"Name\", {})", name);
            ec2.createTags(createTagsRequest);
        }
        return instance;
    }

    public Instance startInstance(String instanceId) {
        StartInstancesRequest startInstancesRequest =
                new StartInstancesRequest().withInstanceIds(instanceId);
        logger.debug("startInstance(): calling ec2.startInstances({})", instanceId);
        ec2.startInstances(startInstancesRequest);
        return getInstance(instanceId);
    }

    public Instance waitForStartup(String instanceId, long timeoutMillis) throws TimeoutException,
            InterruptedException {

        Instance instance = getInstance(instanceId);
        String state = instance.getState().getName();
        if ("running".equals(state)) {
            return instance;
        }
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            logger.debug("waiting for instance to start...");
            Thread.sleep(5000);
            instance = getInstance(instance.getInstanceId());
            state = instance.getState().getName();
            if ("running".equals(state)) {
                return instance;
            }
        }
        throw new TimeoutException();
    }

    public Instance getInstance(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest =
                new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult describeInstancesResult =
                ec2.describeInstances(describeInstancesRequest);
        List<Instance> instances = new ArrayList<Instance>();
        for (Reservation reservation : describeInstancesResult.getReservations()) {
            instances.addAll(reservation.getInstances());
        }
        assert instances.size() == 1;
        return instances.get(0);
    }

    public Instance getInstanceForName(String name) {
        List<Instance> foundInstances = new ArrayList<Instance>();
        logger.debug("getInstanceForName(): calling ec2.describeInstances()");
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> instances = new HashSet<Instance>();

        for (Reservation reservation : reservations) {
            for (Instance instance : reservation.getInstances()) {
                String state = instance.getState().getName();
                if ("shutting-down".equals(state) || "terminated".equals(state)) {
                    continue;
                }
                for (Tag tag : instance.getTags()) {
                    if (tag.getKey().equals("Name") && tag.getValue().equals(name)) {
                        foundInstances.add(instance);
                    }
                }
                instances.add(instance);
            }
        }
        if (foundInstances.size() == 0) {
            return null;
        } else if (foundInstances.size() == 1) {
            return foundInstances.get(0);
        } else {
            throw new IllegalStateException("found more than one instance with name '"
                    + name + "'");
        }
    }

    public void terminateInstanceForName(String name) {
        Instance instance = getInstanceForName(name);
        if (instance == null) {
            return;
        }
        TerminateInstancesRequest terminateInstancesRequest =
                new TerminateInstancesRequest().withInstanceIds(instance.getInstanceId());
        logger.debug("terminateInstanceForName(): calling ec2.terminateInstances({})",
                instance.getInstanceId());
        ec2.terminateInstances(terminateInstancesRequest);
    }

    public void terminateInstanceForId(String instanceId) {
        TerminateInstancesRequest terminateInstancesRequest =
                new TerminateInstancesRequest().withInstanceIds(instanceId);
        logger.debug("terminateInstanceForId(): calling ec2.terminateInstances({})", instanceId);
        ec2.terminateInstances(terminateInstancesRequest);
    }

    public SecurityGroup getOrCreateSecurityGroup(String groupName) {
        SecurityGroup securityGroup = getSecurityGroup(groupName);
        if (securityGroup == null) {
            CreateSecurityGroupRequest createRequest =
                    new CreateSecurityGroupRequest(groupName, groupName);
            ec2.createSecurityGroup(createRequest);
            return getSecurityGroup(groupName);
        } else {
            return securityGroup;
        }
    }

    public SecurityGroup getSecurityGroup(String groupName) {
        DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest()
                .withGroupNames(groupName);
        try {
            DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(request);
            return result.getSecurityGroups().get(0);
        } catch (AmazonServiceException e) {
            return null;
        }
    }

    public void syncInboundRules(SecurityGroup securityGroup, List<IpPermission> ipPermissions) {
        List<WrappedIpPermission> revokeWrappedIpPermissions =
                wrap(securityGroup.getIpPermissions());
        revokeWrappedIpPermissions.removeAll(wrap(ipPermissions));
        List<WrappedIpPermission> authorizeWrappedIpPermissions = wrap(ipPermissions);
        authorizeWrappedIpPermissions.removeAll(wrap(securityGroup.getIpPermissions()));

        // revoke must be done first in case one of multiple UserIdGroupPairs for
        // a single IpPermission is being revoked
        if (!revokeWrappedIpPermissions.isEmpty()) {
            RevokeSecurityGroupIngressRequest request =
                    new RevokeSecurityGroupIngressRequest(securityGroup.getGroupName(),
                            new ArrayList<IpPermission>(unwrap(revokeWrappedIpPermissions)));
            ec2.revokeSecurityGroupIngress(request);
        }
        if (!authorizeWrappedIpPermissions.isEmpty()) {
            AuthorizeSecurityGroupIngressRequest request =
                    new AuthorizeSecurityGroupIngressRequest(securityGroup.getGroupName(),
                            new ArrayList<IpPermission>(unwrap(authorizeWrappedIpPermissions)));
            ec2.authorizeSecurityGroupIngress(request);
        }
    }

    public User getOrCreateUser(String username) {
        GetUserRequest getUserRequest = new GetUserRequest();
        getUserRequest.setUserName(username);
        try {
            return iam.getUser(getUserRequest).getUser();
        } catch (NoSuchEntityException e) {
            CreateUserRequest createUserRequest = new CreateUserRequest(username);
            return iam.createUser(createUserRequest).getUser();
        }
    }

    public void deleteExistingAccessKeys(String username) {
        ListAccessKeysRequest listAccessKeysRequest = new ListAccessKeysRequest();
        listAccessKeysRequest.setUserName(username);
        ListAccessKeysResult result = iam.listAccessKeys(listAccessKeysRequest);
        for (AccessKeyMetadata accessKeyMetadata : result.getAccessKeyMetadata()) {
            DeleteAccessKeyRequest deleteAccessKeyRequest = new DeleteAccessKeyRequest();
            deleteAccessKeyRequest.setUserName(username);
            deleteAccessKeyRequest.setAccessKeyId(accessKeyMetadata.getAccessKeyId());
            iam.deleteAccessKey(deleteAccessKeyRequest);
        }
    }

    public AccessKey createAccessKey(final String username) {
        CreateAccessKeyRequest request = new CreateAccessKeyRequest();
        request.setUserName(username);
        CreateAccessKeyResult result = iam.createAccessKey(request);
        return result.getAccessKey();
    }

    public void putUserPolicy(String username, String userPolicyResourcePath) throws IOException {
        String policyDocument =
                IOUtils.toString(Ec2Service.class.getResourceAsStream(userPolicyResourcePath));
        PutUserPolicyRequest x = new PutUserPolicyRequest(username, username, policyDocument);
        iam.putUserPolicy(x);
    }

    private List<WrappedIpPermission> wrap(List<IpPermission> ipPermissions) {
        List<WrappedIpPermission> wrappedIpPermissions = new ArrayList<WrappedIpPermission>();
        for (IpPermission userIdGroupPair : ipPermissions) {
            wrappedIpPermissions.add(new WrappedIpPermission(userIdGroupPair));
        }
        return wrappedIpPermissions;
    }

    private Collection<? extends IpPermission> unwrap(List<WrappedIpPermission> wrappedIpPermissions) {
        List<IpPermission> ipPermissions = new ArrayList<IpPermission>();
        for (WrappedIpPermission wrappedIpPermission : wrappedIpPermissions) {
            for (UserIdGroupPair userIdGroupPair : wrappedIpPermission.ipPermission
                    .getUserIdGroupPairs()) {
                userIdGroupPair.setGroupId(null);
            }
            ipPermissions.add(wrappedIpPermission.ipPermission);
        }
        return ipPermissions;
    }

    private static class WrappedIpPermission {
        private final IpPermission ipPermission;
        public WrappedIpPermission(IpPermission ipPermission) {
            this.ipPermission = ipPermission;
        }
        @Override
        public int hashCode() {
            return Objects.hashCode(ipPermission.getFromPort(),
                    ipPermission.getToPort(),
                    ipPermission.getIpProtocol(),
                    ipPermission.getIpRanges(),
                    ipPermission.getUserIdGroupPairs());
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof WrappedIpPermission)) {
                return false;
            }
            IpPermission o1 = ipPermission;
            IpPermission o2 = ((WrappedIpPermission) o).ipPermission;
            return Objects.equal(o1.getFromPort(), o2.getFromPort())
                    && Objects.equal(o1.getToPort(), o2.getToPort())
                    && Objects.equal(o1.getIpProtocol(), o2.getIpProtocol())
                    && Objects.equal(o1.getIpRanges(), o2.getIpRanges())
                    && equals(o1.getUserIdGroupPairs(), o2.getUserIdGroupPairs());
        }
        private boolean equals(List<UserIdGroupPair> o1, List<UserIdGroupPair> o2) {
            return wrap(o1).equals(wrap(o2));
        }
        private List<WrappedUserIdGroupPair> wrap(List<UserIdGroupPair> userIdGroupPairs) {
            List<WrappedUserIdGroupPair> wrappedUserIdGroupPairs =
                    new ArrayList<WrappedUserIdGroupPair>();
            for (UserIdGroupPair userIdGroupPair : userIdGroupPairs) {
                wrappedUserIdGroupPairs.add(new WrappedUserIdGroupPair(userIdGroupPair));
            }
            return wrappedUserIdGroupPairs;
        }
    }

    private static class WrappedUserIdGroupPair {
        private final UserIdGroupPair userIdGroupPair;
        public WrappedUserIdGroupPair(UserIdGroupPair userIdGroupPair) {
            this.userIdGroupPair = userIdGroupPair;
        }
        @Override
        public int hashCode() {
            return Objects.hashCode(userIdGroupPair.getGroupName(), userIdGroupPair.getUserId());
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof WrappedUserIdGroupPair)) {
                return false;
            }
            UserIdGroupPair o1 = userIdGroupPair;
            UserIdGroupPair o2 = ((WrappedUserIdGroupPair) o).userIdGroupPair;
            // only comparing name since groupId is empty for the objects built locally
            // (as opposed to the ones pulled down via describeSecurityGroups
            return Objects.equal(o1.getGroupName(), o2.getGroupName())
                    && Objects.equal(o1.getUserId(), o2.getUserId());
        }
    }
}
