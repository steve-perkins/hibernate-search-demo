<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
Design by Free CSS Templates
http://www.freecsstemplates.org
Released for free under a Creative Commons Attribution 3.0 License

Name       : Unofficial Channels
Description: A two-column, fixed-width design with a bright color scheme.
Version    : 1.0
Released   : 20120723
-->
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta name="keywords" content="" />
		<meta name="description" content="" />
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<title>
			VAPORware Marketplace
		</title>

		<link type="text/css" href="css/custom-theme/jquery-ui-1.8.24.custom.css" rel="stylesheet" />
		<link href="http://fonts.googleapis.com/css?family=Arvo" rel="stylesheet" type="text/css" />
		<link rel="stylesheet" type="text/css" href="style.css" />

		<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
		<script type="text/javascript" src="js/jquery-ui-1.8.24.custom.min.js"></script>
		<script language="JavaScript">

			// JavaScript to execute after the page finishes loading
			$(function() {

				// Put the active cursor on the search box
				$("#searchString").focus();

			});		
			
			// When the search button is clicked, ensure that the search box is not empty and then submit the search form
			function submitSearchForm() {
				var searchString = $("#searchString").val();
				if(searchString == null || searchString.replace(/^\s+|\s+$/g, '') == "") {
					alert("You must enter some search words");
				} else {
					$("#searchForm").submit();
				}
			}
			
			// When the user selects a new sort order, re-do the search with the selected sorting
			function submitSortedSearch() {
				$("#sortedSearchForm").submit();
			}

			// When the user clicks the "Full Details" button for an app... call a RESTful service to fetch the full detail for
			// that app (including its supported devices and customer reviews), and display it in a jQuery UI modal.
			function showAppDetails(appId) {
				
				// Call the RESTful service, passing this app id
				var request = $.ajax({
					url: "rest/appById/" + appId,
					dataType: "json",
					
					// Display the jQuery UI  modal
					success: function(app) {
						
						// Update the modal to contain the content for this app
						$("#appDetailName").html(app.name);
						$("#appDetailPrice").html("$" + app.price);
						$("#appDetailDescription").html(app.description);
						$("#appDetailImg").attr("src", "images/apps/" + app.image);
						$("#appDetailInstallButton").attr("onclick", "installApp(" + app.price + ");");
						
						var supportedDevicesHTML = "";
						for(var deviceIndex = 0; deviceIndex < app.supportedDevices.length; deviceIndex++) {
							var device = app.supportedDevices[deviceIndex];
							supportedDevicesHTML += device.manufacturer + " " + device.name;
							if(deviceIndex + 1 < app.supportedDevices.length) {
								supportedDevicesHTML += ", ";
							}
						}
						$("#appDetailSupportedDevices").html(supportedDevicesHTML);
						
						var customerReviewsHTML = "";
						for(var reviewIndex = 0; reviewIndex < app.customerReviews.length; reviewIndex++) {
							var customerReview = app.customerReviews[reviewIndex];
							customerReviewsHTML += "<b>" + customerReview.stars + " out of 5 stars</b> (user: <i>" + customerReview.username + "</i>)<br/>" + customerReview.comments + "<br/><br/>";
						}
						$("#appDetailCustomerReviews").html(customerReviewsHTML);
						
						$("#appDetail").dialog({
							width: 650,
							modal: true
						});
					},
					
					fail: function(jqXHR, textStatus) {
						alert( "Request failed: " + textStatus );
					}
				});
			}
			
			// When the "Install" button is clicked for an app, display an alert indicating the the app was purchased			
			function installApp(price) {
				alert("Thank you for pretending to install this pretend app!\nA pretend $" + price.toFixed(2) + " has been charged to your pretend account.");
			}
		</script>
	</head>
	<body>
		<div id="bg1"></div>
		<div id="bg2"></div>
		<div id="outer">
			<div id="header">
				<div id="logo">
					<h1><a href="index.html"><i>VAPORware</i> Marketplace</a></h1>
				</div>
				<form id="searchForm" action="search" method="post">
					<div id="search">
						<div>
							<!-- Allow for filtering of apps by a particular supported device.  If a device filter is already being used, the pre-select that device in the list. -->
							<label for="selectedDevice">Search by device:</label>
							<select id="selectedDevice" name="selectedDevice">
								<option value="all" ${selectedDevice == 'all' ? 'selected="selected"' : ''}>All Devices</option>
								<option value="xPhone" ${selectedDevice == 'xPhone' ? 'selected="selected"' : ''}>Orange xPhone</option>
								<option value="xTablet" ${selectedDevice == 'xTablet' ? 'selected="selected"' : ''}>Orange xTablet</option>
								<option value="Solar System Phone" ${selectedDevice == 'Solar System Phone' ? 'selected="selected"' : ''}>Song-Sung Solar System Phone</option>
								<option value="Flame Book Reader" ${selectedDevice == 'Flame Book Reader' ? 'selected="selected"' : ''}>Jungle Flame Book Reader</option>
								<option value="Personal Computer" ${selectedDevice == 'Personal Computer' ? 'selected="selected"' : ''}>Personal Computer</option>
							</select>
							<input class="text" id="searchString" name="searchString" size="32" maxlength="64" value='${fn:replace(searchString, "\"", "&quot;")}' />
							<img src="images/search.png" style="margin: -13px;" onclick="submitSearchForm()"/>
						</div>
					</div>
				</form>
			</div>
			<div id="banner">
				<div class="captions">
					<h2><i>The</i>&nbsp;&nbsp;source for apps that may or may not ever be developed</h2>
				</div>
				<img src="images/banner.jpg" alt="" height="150" width="1180"/>
			</div>
			<div id="main">
				<div id="sidebar">
					<div class="box">
						<!-- 
							Dynamically build a list of categories and per-category hit totals found in the current results. 
							Each category should be a link back to this page, with parameters setting the selected category and 
							retaining all other state.
						-->
						<h3>Categories</h3>
							<ul>
								<c:forEach items="${categories}" var="category" varStatus="loop">
								<li ${loop.index == 0 ? 'class="first"' : ''}>
									<c:if test="${selectedCategory == category.key}"><b></c:if>
									<a href='<c:url value="search">
										<c:param name="searchString" value="${searchString}"/>
										<c:param name="selectedDevice" value="${selectedDevice}"/>
										<c:param name="selectedCategory" value="${category.key}"/>
										<c:param name="selectedPriceRange" value="${selectedPriceRange}"/>
										<c:param name="sortField" value="${sortField}"/>
										</c:url>'>${category.key}</a> (${category.value})
									<c:if test="${selectedCategory == category.key}"></b></c:if>
								</li>								
								</c:forEach>
								<!-- This "all categories" nullifies the category selection, while retaining all other state. -->
								<li>
									<c:if test="${selectedCategory == 'all'}"><b></c:if>								
									<a href='<c:url value="search">
										<c:param name="searchString" value="${searchString}"/>
										<c:param name="selectedDevice" value="${selectedDevice}"/>
										<c:param name="selectedPriceRange" value="${selectedPriceRange}"/>
										<c:param name="sortField" value="${sortField}"/>
										</c:url>'>all</a> (${resultSize})
									<c:if test="${selectedCategory == 'all'}"></b></c:if>								
								</li>								
							</ul>
						<!-- 
							Dynamically build a list of price bands found in the current results.  Each price band should be a link 
							back to this page, with parameters setting the selected price band and retaining all other state.
						-->
						<h3>Price Range</h3>
							<ul>
								<c:forEach items="${priceRanges}" var="priceRange" varStatus="loop">
								<c:set var="readablePriceRange" scope="request"><c:if test="${priceRange.key == '[, 1.0)'}">below $1</c:if><c:if test="${priceRange.key == '[1.0, 5.0]'}">$1 - $5</c:if><c:if test="${priceRange.key == '(5.0, ]'}">above $5</c:if></c:set>
								<li ${loop.index == 0 ? 'class="first"' : ''}>
									<c:if test="${selectedPriceRange == priceRange.key}"><b></c:if>
									<a href='<c:url value="search">
										<c:param name="searchString" value="${searchString}"/>
										<c:param name="selectedDevice" value="${selectedDevice}"/>
										<c:param name="selectedCategory" value="${selectedCategory}"/>
										<c:param name="selectedPriceRange" value="${priceRange.key}"/>
										<c:param name="sortField" value="${sortField}"/>
										</c:url>'>${readablePriceRange}</a>
									<c:if test="${selectedPriceRange == priceRange.key}"></b></c:if>
								</li>								
								</c:forEach>
								<!-- This "all prices" nullifies the price band selection, while retaining all other state. -->
								<li>
									<c:if test="${selectedPriceRange == 'all'}"><b></c:if>
									<a href='<c:url value="search">
										<c:param name="searchString" value="${searchString}"/>
										<c:param name="selectedDevice" value="${selectedDevice}"/>
										<c:param name="selectedCategory" value="${selectedCategory}"/>
										<c:param name="sortField" value="${sortField}"/>
										</c:url>'>all</a>
									<c:if test="${selectedPriceRange == 'all'}"></b></c:if>
								</li>								
							</ul>
					</div>
				</div>
				<div id="content">
					<div class="box">
						<table>
							<tr>
								<td valign="top"><h2>Search Results</h2></td>
								<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
								<td valign="top">
								
									<!-- 
										A <select> pulldown showing all of the available sort order options, with the current option pre-selected.
										When the selection changes, a new search will be peformed with the selected sort order... and all other 
										state retained.
									-->
									<form id="sortedSearchForm" action="search" method="post">
										<label for="sortField">Sort by:</label>
										<select id="sortField" name="sortField" onchange="submitSortedSearch()">
											<option value="relevance" ${sortField == 'relevance' ? 'selected="selected"' : ''}>Relevance</option>
											<option value="name" ${sortField == 'name' ? 'selected="selected"' : ''}>Name (A-Z)</option>
											<option value="name-reverse" ${sortField == 'name-reverse' ? 'selected="selected"' : ''}>Name (Z-A)</option>
										</select>
										<input type="hidden" name="searchString" value='${fn:replace(searchString, "\"", "&quot;")}' />							
										<input type="hidden" name="selectedDevice" value="${selectedDevice}" />							
										<c:if test="${selectedCategory != null}"><input type="hidden" name="selectedCategory" value="${selectedCategory}" /></c:if>							
										<c:if test="${selectedPriceRange != null}"><input type="hidden" name="selectedPriceRange" value="${selectedPriceRange}" /></c:if>							
									</form>
								</td>
								<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
								<td valign="top">
								
									<!-- 
										Pagination.  When a search query returns more than 5 results, the controller servlet forwards only a 
										batch of 5 to this JSP.  The code below displays the total number of results, and where the current batch 
										is among that number.  "Previous" and "Next" links send the user to the previous or next 5 result batch.
									-->
									${firstResult+1}-${firstResult+5 < resultSize ? firstResult+5 : resultSize} of ${resultSize} results
									&nbsp;&nbsp;&nbsp;
									<c:if test="${firstResult > 0}">
										(<a href='<c:url value="search">
										<c:param name="searchString" value="${searchString}"/>
										<c:param name="selectedDevice" value="${selectedDevice}"/>
										<c:if test="${selectedCategory != null}"><c:param name="selectedCategory" value="${selectedCategory}"/></c:if>
										<c:param name="sortField" value="${sortField}"/>
										<c:param name="firstResult" value="${firstResult-5}"/>
										</c:url>'>prev</a>)
									</c:if>
									<c:if test="${firstResult+5 < resultSize}">
										(<a href='<c:url value="search">
										<c:param name="searchString" value="${searchString}"/>
										<c:param name="selectedDevice" value="${selectedDevice}"/>
										<c:if test="${selectedCategory != null}"><c:param name="selectedCategory" value="${selectedCategory}"/></c:if>
										<c:param name="sortField" value="${sortField}"/>
										<c:param name="firstResult" value="${firstResult+5}"/>
										</c:url>'>next</a>)
									</c:if>
								</td>
							</tr>
						</table>
						
						<!-- Iterate through the search results inserted into this request by the controller servlet, displaying a row for each. -->
						<table style="width: 100%; margin-left: auto; margin-right: auto;">
						<c:forEach items="${apps}" var="app" varStatus="loop">
						<tr>
							<td style="width: 25%; text-align: center; vertical-align: middle;" rowspan="2"><img src="images/apps/${app.image}"/></td>
							<td style="height: 1em; width: 75%; text-align: left; vertical-align: bottom; border-bottom: thin dotted black;">
								<b>${app.name}</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
								<input type="button" id="appDetailButton_${loop.index}" value="Full Details" onclick="showAppDetails(${app.id})" />
							</td>							
						</tr>
						<tr>
							<td style="text-align: left; vertical-align: top; padding-bottom: 30px;">${app.description}</td>
						</tr>
						</c:forEach>
						</table>
						
						<!-- 
							Pop-up modal, showing the full details for an app.  The <div> representing this modal is hidden by
							on page load, but is used to build a jQuery UI dialog when the "Full Details" button for an app is clicked.
						-->
						<div style="font-size:10px; display:none;">
							<div id="appDetail" title="App Details">
								<img id="appDetailImg" src="" style="float:left; margin:10px;" />
								<b><span id="appDetailName"></span></b>&nbsp;&nbsp;&nbsp;&nbsp;<span id="appDetailPrice"></span>&nbsp;&nbsp;&nbsp;&nbsp;
								<input id="appDetailInstallButton" type="button" value="Install" /><hr/>
								<br/><span id="appDetailDescription"></span><br/><br/><hr/>
								Supported devices: <span id="appDetailSupportedDevices"></span>
								<hr/><br/>
								Customer Reviews: <br/><br/><span id="appDetailCustomerReviews"></span><br/><br/>
							</div>
						</div>
						
					</div><br class="clear" />
				</div><br class="clear" />
			</div>
		</div>
		<div id="copyright">
			This application uses HTML/CSS design from <a href="http://www.freecsstemplates.org/">freecsstemplates.org</a>, 
			and photos by <a href="http://fotogrph.com/">Fotogrph</a>, both made available under the 
			<a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution license</a>.
		</div>
	</body>
</html>
