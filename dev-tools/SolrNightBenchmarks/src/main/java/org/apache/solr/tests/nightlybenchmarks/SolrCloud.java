package org.apache.solr.tests.nightlybenchmarks;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jgit.api.errors.GitAPIException;

public class SolrCloud {

	public int solrNodes;
	public String shards;
	public String replicas;
	public String port;
	public Zookeeper zookeeperNode;
	public String zookeeperPort;
	public String zookeeperIp;
	public String commitId;
	public String collectionName;
	public String configName;
	public SolrNode masterNode;
	public List<SolrNode> nodes;
	public String url;
	public String host;
	public boolean createADefaultCollection;
	public Map<String, String> returnMapCreateCollection;

	public SolrCloud(int solrNodes, String shards, String replicas, String commitId, String configName, String host,
			boolean creatADefaultCollection) {
		super();
		this.solrNodes = solrNodes;
		this.shards = shards;
		this.replicas = replicas;
		this.commitId = commitId;
		this.configName = configName;
		this.host = host;
		this.collectionName = "Collection_" + UUID.randomUUID();
		this.createADefaultCollection = creatADefaultCollection;
		nodes = new LinkedList<SolrNode>();
		this.init();
	}

	private void init() {

		try {

			zookeeperNode = new Zookeeper();
			int initValue = zookeeperNode.doAction(ZookeeperAction.ZOOKEEPER_START);
			if (initValue == 0) {
				this.zookeeperIp = zookeeperNode.getZookeeperIp();
				this.zookeeperPort = zookeeperNode.getZookeeperPort();
			} else {
				throw new RuntimeException("Failed to start Zookeeper!");
			}

			for (int i = 1; i <= solrNodes; i++) {

				SolrNode node = new SolrNode(commitId, this.zookeeperIp, this.zookeeperPort, true);
				node.doAction(SolrNodeAction.NODE_START);
				nodes.add(node);
			}

			if (this.createADefaultCollection) {
				returnMapCreateCollection = nodes.get(0).createCollection(this.collectionName, this.configName,
						this.shards, this.replicas);
			}

			this.port = nodes.get(0).port;

			this.url = "http://" + this.host + ":" + this.port + "/solr/" + this.collectionName;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void createCollection(String collectionName, String configName, String shards, String replicas) {
		try {
			nodes.get(0).createCollection(collectionName, configName, shards, replicas);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getuRL() {
		if (createADefaultCollection) {
			return "http://" + this.host + ":" + this.port + "/solr/" + this.collectionName;
		} else {
			return "http://" + this.host + ":" + this.port + "/solr/";
		}
	}

	public String getBaseURL() {
		return "http://" + this.host + ":" + this.port + "/solr/";
	}

	public void shutdown() throws IOException, InterruptedException {
		for (SolrNode node : nodes) {
			node.doAction(SolrNodeAction.NODE_STOP);
			node.cleanup();

		}
		zookeeperNode.doAction(ZookeeperAction.ZOOKEEPER_STOP);
		zookeeperNode.doAction(ZookeeperAction.ZOOKEEPER_CLEAN);
	}

}
