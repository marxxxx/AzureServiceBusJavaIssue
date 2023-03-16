param serviceBusNamespaceName string
param skuName string = 'Standard'
param skuTier string = 'Standard'
param serviceBusQueueName string
param location string = resourceGroup().location

resource sbNamespace 'Microsoft.ServiceBus/namespaces@2021-11-01' = {
  name: serviceBusNamespaceName
  location: location
  sku: {
    name: skuName
    tier: skuTier
  }
  properties: {
    disableLocalAuth: false
    zoneRedundant: false
  }
}

resource sbRootManageSharedAccessKey 'Microsoft.ServiceBus/namespaces/AuthorizationRules@2021-11-01' = {
  parent: sbNamespace
  name: 'RootManageSharedAccessKey'
  properties: {
    rights: [
      'Listen'
      'Manage'
      'Send'
    ]
  }
}

resource sbMainQueue 'Microsoft.ServiceBus/namespaces/queues@2021-11-01' = {
  parent: sbNamespace
  name: serviceBusQueueName
  properties: {
    maxMessageSizeInKilobytes: 256
    lockDuration: 'PT30S'
    maxSizeInMegabytes: 5120
    requiresDuplicateDetection: false
    requiresSession: true
    defaultMessageTimeToLive: 'P1D'
    deadLetteringOnMessageExpiration: true
    enableBatchedOperations: true
    duplicateDetectionHistoryTimeWindow: 'PT10M'
    maxDeliveryCount: 5
    status: 'Active'
    autoDeleteOnIdle: 'P10675199DT2H48M5.4775807S'
    enablePartitioning: false
    enableExpress: false
  }
}

resource sbMainQueue_Listener 'Microsoft.ServiceBus/namespaces/queues/authorizationRules@2021-11-01' = {
  parent: sbMainQueue
  name: 'Listener'
  properties: {
    rights: [
      'Listen'
    ]
  }
}

resource sbMainQueue_Manager 'Microsoft.ServiceBus/namespaces/queues/authorizationRules@2021-11-01' = {
  parent: sbMainQueue
  name: 'Manager'
  properties: {
    rights: [
      'Manage'
      'Listen'
      'Send'
    ]
  }
}

resource sbMainQueue_Sender 'Microsoft.ServiceBus/namespaces/queues/authorizationRules@2021-11-01' = {
  parent: sbMainQueue
  name: 'Sender'
  properties: {
    rights: [
      'Send'
    ]
  }
}
