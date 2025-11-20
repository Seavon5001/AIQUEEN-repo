function doGet() {
  return HtmlService.createHtmlOutputFromFile('index').setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL);
}

function proxy(path, payload) {
  var base = PropertiesService.getScriptProperties().getProperty('AIQ_NODE_URL');
  if (!base) return {ok:false,error:'AIQ_NODE_URL not set'};
  var url = base.replace(/\\/+$/,'') + '/' + path.replace(/^\\/+/, '');
  var options = {method:'post',contentType:'application/json',payload:JSON.stringify(payload),muteHttpExceptions:true};
  var resp = UrlFetchApp.fetch(url, options);
  try { return JSON.parse(resp.getContentText()); } catch(e){ return {ok:false,error:resp.getContentText()}; }
}
