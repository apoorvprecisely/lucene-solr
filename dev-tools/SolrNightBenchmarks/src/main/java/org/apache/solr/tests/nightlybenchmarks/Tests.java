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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.solr.tests.nightlybenchmarks.QueryClient.QueryType;

enum ConfigurationType { STANDALONE, CLOUD }

public class Tests {

	public static SolrCloud cloud;
	public static SolrNode node;
	public static int queryThreadCount = Integer.parseInt(Util.QUERY_THREAD_COUNT);

	
	
	public static boolean createCollectionTestStandalone(String commitID) {

		try {
			SolrNode node = new SolrNode(commitID, "", "", false);

			node.doAction(SolrNodeAction.NODE_START);

			Util.getEnvironmentInformationFromMetricAPI(commitID, node.port);

			BenchmarkReportData.returnStandaloneCreateCollectionMap = node.createCollection("Core-" + UUID.randomUUID(),
					"Collection-" + UUID.randomUUID());

			node.doAction(SolrNodeAction.NODE_STOP);
			node.cleanup();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public static boolean indexingTestsStandalone(String commitID, int numDocuments) {

		try {
			SolrNode node = new SolrNode(commitID, "", "", false);

			node.doAction(SolrNodeAction.NODE_START);
			Util.getEnvironmentInformationFromMetricAPI(commitID, node.port);
			node.createCollection("Core-" + UUID.randomUUID(),
					"Collection-" + UUID.randomUUID());

			SolrIndexingClient client = new SolrIndexingClient("localhost", node.port, node.collectionName, commitID);
			BenchmarkReportData.metricMapStandalone = client.indexAmazonFoodData(numDocuments,
					node.getBaseUrl() + node.collectionName, true);

			node.doAction(SolrNodeAction.NODE_STOP);
			node.cleanup();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	
	public static boolean indexingTestsStandaloneConcurrent(String commitID, int numDocuments) {

		try {
			SolrNode node = new SolrNode(commitID, "", "", false);
			node.doAction(SolrNodeAction.NODE_START);
			node.createCollection("Core-" + UUID.randomUUID(),"Collection-" + UUID.randomUUID());
			SolrIndexingClient client = new SolrIndexingClient("localhost", node.port, node.collectionName, commitID);
			

			BenchmarkReportData.metricMapStandaloneConcurrent2 = client.indexAmazonFoodData(numDocuments,
					node.getBaseUrlC(), node.collectionName, 10000, 2, TestType.STANDALONE_INDEXING_THROUGHPUT_CONCURRENT_2, true);
			BenchmarkReportData.metricMapStandaloneConcurrent4 = client.indexAmazonFoodData(numDocuments,
					node.getBaseUrlC(), node.collectionName, 10000, 4, TestType.STANDALONE_INDEXING_THROUGHPUT_CONCURRENT_4, true);
			BenchmarkReportData.metricMapStandaloneConcurrent6 = client.indexAmazonFoodData(numDocuments,
					node.getBaseUrlC(), node.collectionName, 10000, 6, TestType.STANDALONE_INDEXING_THROUGHPUT_CONCURRENT_6, true);
			BenchmarkReportData.metricMapStandaloneConcurrent8 = client.indexAmazonFoodData(numDocuments,
					node.getBaseUrlC(), node.collectionName, 10000, 8, TestType.STANDALONE_INDEXING_THROUGHPUT_CONCURRENT_8, true);
			BenchmarkReportData.metricMapStandaloneConcurrent10 = client.indexAmazonFoodData(numDocuments,
					node.getBaseUrlC(), node.collectionName, 10000, 10, TestType.STANDALONE_INDEXING_THROUGHPUT_CONCURRENT_10, true);

			
			node.doAction(SolrNodeAction.NODE_STOP);
			node.cleanup();
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public static boolean indexingTestsCloudSerial(String commitID, int numDocuments) {

		try {

			SolrCloud cloud = new SolrCloud(5, "2", "2", commitID, null, "localhost", true);
			Tests.cloud = cloud;
			SolrIndexingClient cloudClient = new SolrIndexingClient("localhost", cloud.port, cloud.collectionName,
					commitID);
			BenchmarkReportData.metricMapCloudSerial = cloudClient.indexAmazonFoodData(numDocuments, cloud.getuRL(),
					cloud.zookeeperIp, cloud.zookeeperPort, cloud.collectionName, true);

			cloud.shutdown();
			BenchmarkReportData.returnCloudCreateCollectionMap = cloud.returnMapCreateCollection;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public static boolean indexingTestsCloudConcurrent(String commitID, int numDocuments) {

		try {

			SolrCloud cloud = new SolrCloud(5, "2", "2", commitID, null, "localhost", true);
			Tests.cloud = cloud;
			SolrIndexingClient cloudClient = new SolrIndexingClient("localhost", cloud.port, cloud.collectionName,
					commitID);

			BenchmarkReportData.metricMapCloudConcurrent2 = cloudClient.indexAmazonFoodData(numDocuments,
					cloud.getuRL(), cloud.collectionName, 10000, 2, TestType.CLOUD_INDEXING_THROUGHPUT_CONCURRENT_2, true);
			BenchmarkReportData.metricMapCloudConcurrent4 = cloudClient.indexAmazonFoodData(numDocuments,
					cloud.getuRL(), cloud.collectionName, 10000, 4, TestType.CLOUD_INDEXING_THROUGHPUT_CONCURRENT_4, true);
			BenchmarkReportData.metricMapCloudConcurrent6 = cloudClient.indexAmazonFoodData(numDocuments,
					cloud.getuRL(), cloud.collectionName, 10000, 6, TestType.CLOUD_INDEXING_THROUGHPUT_CONCURRENT_6, true);
			BenchmarkReportData.metricMapCloudConcurrent8 = cloudClient.indexAmazonFoodData(numDocuments,
					cloud.getuRL(), cloud.collectionName, 10000, 8, TestType.CLOUD_INDEXING_THROUGHPUT_CONCURRENT_8, true);
			BenchmarkReportData.metricMapCloudConcurrent10 = cloudClient.indexAmazonFoodData(numDocuments,
					cloud.getuRL(), cloud.collectionName, 10000, 10, TestType.CLOUD_INDEXING_THROUGHPUT_CONCURRENT_10, true);

			cloud.shutdown();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	
	private static Map<String, String> numericQueryTests(String commitID, QueryType queryType, int numberOfThreads,
			int secondsToWait, long delayEstimationBySeconds, ConfigurationType confType, String baseURL, String collectionName) {

		try {
			QueryClient.reset();
			
			ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
			LinkedList<QueryClient> list = new LinkedList<QueryClient>();

			for (int i = 0; i < numberOfThreads; i++) {
				QueryClient client = new QueryClient(baseURL, 10000, 10, collectionName, queryType,
						numberOfThreads, delayEstimationBySeconds);
				list.add(client);
			}

			QueryClient.running = true;

			for (int i = 0; i < numberOfThreads; i++) {
				executorService.execute(list.get(i));
			}

			Thread.sleep(secondsToWait * 1000);

			QueryClient.running = false;

			executorService.shutdownNow();

			Thread.sleep(5000);

			Map<String, String> returnMap = new HashMap<String, String>();

			Date dNow = new Date();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

			returnMap.put("TimeStamp", ft.format(dNow));
			returnMap.put("CommitID", commitID);
			returnMap.put("TotalQueriesExecuted", "" + QueryClient.queryCount);
			returnMap.put("QueriesPerSecond",
					"" + (double) (QueryClient.queryCount / (secondsToWait - delayEstimationBySeconds)));
			returnMap.put("MinQTime", "" + QueryClient.minQtime);
			returnMap.put("MaxQTime", "" + QueryClient.maxQtime);
			returnMap.put("QueryFailureCount", "" + QueryClient.queryFailureCount);
			returnMap.put("75thQtime", "" + QueryClient.getNthPercentileQTime(75));
			returnMap.put("95thQtime", "" + QueryClient.getNthPercentileQTime(95));
			returnMap.put("99thQtime", "" + QueryClient.getNthPercentileQTime(99));
			returnMap.put("99.9thQtime", "" + QueryClient.getNthPercentileQTime(99.9));

			Util.postMessage(returnMap.toString(), MessageType.RED_TEXT, false);
			QueryClient.reset();

			return returnMap;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static String setUpCloudForFeatureTests(String commitID, int documentCount, int solrNodes, String shards,
			String replicas, int numDocuments) {

		SolrCloud cloud = new SolrCloud(solrNodes, shards, replicas, commitID, null, "localhost", true);
		Tests.cloud = cloud;
		SolrIndexingClient cloudClient = new SolrIndexingClient("localhost", cloud.port, cloud.collectionName,
				commitID);
		cloudClient.indexAmazonFoodData(documentCount, cloud.getuRL(), cloud.collectionName, numDocuments, 10, TestType.CLOUD_INDEXING_THROUGHPUT_CONCURRENT_10, false);
		
		return cloud.port;
	}
	
	private static String setUpStandaloneNodeForFeatureTests(String commitID, int numDocuments) {
		
		try {
			SolrNode snode = new SolrNode(commitID, "", "", false);
			snode.doAction(SolrNodeAction.NODE_START);
			snode.createCollection("Core-" + UUID.randomUUID(),"Collection-" + UUID.randomUUID());
			
			SolrIndexingClient client = new SolrIndexingClient("localhost", snode.port, snode.collectionName, commitID);
			client.indexAmazonFoodData(numDocuments, snode.getBaseUrl() + snode.collectionName, false);
			
			node = snode;
			
			return node.port;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return "0";
	}

	private static void shutDownCloud() throws IOException, InterruptedException {
		cloud.shutdown();
	}

	private static void shutDownStandalone() throws IOException, InterruptedException {
		node.doAction(SolrNodeAction.NODE_STOP);
		node.cleanup();
	}
	
	@SuppressWarnings("deprecation")
	public static void runNumericTestsCloud() throws IOException, InterruptedException {
		
		String port = Tests.setUpCloudForFeatureTests(Util.COMMIT_ID, 50000, 1, "2", "2", 10000);

		Thread numericQueryTNQMetricC = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.TERM_NUMERIC_QUERY_CLOUD, port));
		numericQueryTNQMetricC.start();
		
			BenchmarkReportData.numericQueryTNQMetricC = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.TERM_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.CLOUD, Tests.cloud.getBaseURL(),
					Tests.cloud.collectionName);
		
			numericQueryTNQMetricC.stop();

		Thread numericQueryRNQMetricC = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.RANGE_NUMERIC_QUERY_CLOUD, port));
		numericQueryRNQMetricC.start();
		
			BenchmarkReportData.numericQueryRNQMetricC = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.RANGE_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.CLOUD, Tests.cloud.getBaseURL(),
					Tests.cloud.collectionName);

			numericQueryRNQMetricC.stop();

		Thread numericQueryLNQMetricC = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.LT_NUMERIC_QUERY_CLOUD, port));
		numericQueryLNQMetricC.start();

		
			BenchmarkReportData.numericQueryLNQMetricC = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.LESS_THAN_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.CLOUD, Tests.cloud.getBaseURL(),
					Tests.cloud.collectionName);

		
		numericQueryLNQMetricC.stop();
		
		Thread numericQueryGNQMetricC = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.GT_NUMERIC_QUERY_CLOUD, port));
		numericQueryGNQMetricC.start();
	
		
			BenchmarkReportData.numericQueryGNQMetricC = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.GREATER_THAN_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.CLOUD, Tests.cloud.getBaseURL(),
					Tests.cloud.collectionName);
		
		numericQueryGNQMetricC.stop();

		Thread numericQueryANQMetricC = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.AND_NUMERIC_QUERY_CLOUD, port));
		numericQueryANQMetricC.start();
		
		
			BenchmarkReportData.numericQueryANQMetricC = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.AND_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.CLOUD, Tests.cloud.getBaseURL(),
					Tests.cloud.collectionName);

		numericQueryANQMetricC.stop();
		
		Thread numericQueryONQMetricC = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.OR_NUMERIC_QUERY_CLOUD, port));
		numericQueryONQMetricC.start();
		
		
			BenchmarkReportData.numericQueryONQMetricC = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.OR_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.CLOUD, Tests.cloud.getBaseURL(),
					Tests.cloud.collectionName);		
		
		numericQueryONQMetricC.stop();
		
		
		Tests.shutDownCloud();

	}
	
	@SuppressWarnings("deprecation")
	public static void runNumericQueryTestsStandalone() throws IOException, InterruptedException {

		
		String port = Tests.setUpStandaloneNodeForFeatureTests(Util.COMMIT_ID, 10000);

		Thread numericQueryTNQMetricS = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.TERM_NUMERIC_QUERY_STANDALONE, port));
		numericQueryTNQMetricS.start();
		
			BenchmarkReportData.numericQueryTNQMetricS = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.TERM_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.STANDALONE, Tests.node.getBaseUrl(),
					Tests.node.collectionName);

		numericQueryTNQMetricS.stop();
		
		Thread numericQueryRNQMetricS = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.RANGE_NUMERIC_QUERY_STANDALONE, port));
		numericQueryRNQMetricS.start();
		
			BenchmarkReportData.numericQueryRNQMetricS = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.RANGE_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.STANDALONE, Tests.node.getBaseUrl(),
					Tests.node.collectionName);

		numericQueryRNQMetricS.stop();

		Thread numericQueryLNQMetricS = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.LT_NUMERIC_QUERY_STANDALONE, port));
		numericQueryLNQMetricS.start();

		
			BenchmarkReportData.numericQueryLNQMetricS = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.LESS_THAN_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.STANDALONE, Tests.node.getBaseUrl(),
					Tests.node.collectionName);

		numericQueryLNQMetricS.stop();
		

		Thread numericQueryGNQMetricS = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.GT_NUMERIC_QUERY_STANDALONE, port));
		numericQueryGNQMetricS.start();

		
			BenchmarkReportData.numericQueryGNQMetricS = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.GREATER_THAN_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.STANDALONE, Tests.node.getBaseUrl(),
					Tests.node.collectionName);

		numericQueryGNQMetricS.stop();
		
		Thread numericQueryANQMetricS = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.AND_NUMERIC_QUERY_STANDALONE, port));
		numericQueryANQMetricS.start();
		
			BenchmarkReportData.numericQueryANQMetricS = Tests.numericQueryTests(Util.COMMIT_ID,
					QueryType.AND_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.STANDALONE, Tests.node.getBaseUrl(),
					Tests.node.collectionName);

		numericQueryANQMetricS.stop();
		
		Thread numericQueryONQMetricS = new Thread(new MetricCollector(Util.COMMIT_ID, TestType.OR_NUMERIC_QUERY_STANDALONE, port));
		numericQueryONQMetricS.start();
		
		BenchmarkReportData.numericQueryONQMetricS = Tests.numericQueryTests(Util.COMMIT_ID,
				QueryType.OR_NUMERIC_QUERY, queryThreadCount, 180, 120, ConfigurationType.STANDALONE, Tests.node.getBaseUrl(),
				Tests.node.collectionName);
		
		numericQueryONQMetricS.stop();
		
		
		Tests.shutDownStandalone();

	}

}