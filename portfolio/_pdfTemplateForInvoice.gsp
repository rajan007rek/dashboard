<div style="width: 97%; background: #fff; margin: 0px 20px 0px;">
    <div style="width: 800px; display: block; margin: 0 auto; padding: 30px 0px; font-family: arial;">
        <div style="width: 83%; overflow: hidden; margin-bottom: 5px;">
            <asset:image border="0" alt=" Oodles Technologies- Software Development Company India" src="dashboard/project/invoice-oodles-logo.png" itemprop="logo"/>
            <h2 style="float: right; margin: 13px 0 0; line-height: 25px; color: #2d82c3; font-size: 20px; font-weight: bold;">INVOICE</h2>
        </div>
        <div style="width: 83%; font-size: 14px; margin-right: 0px;margin-bottom: 5px; color: #666666;">
                <table style="border: 2px solid #666; margin: 10px 0 0 0; padding: 0px; border-collapse: collapse; width: 100%;">
                    <tbody>
                    <th style="padding: 5px 10px; border: 1px solid #666;">Invoice&#160;number</th>
                    <th style="padding: 5px 10px; border: 1px solid #666;">Invoice&#160;date</th>
                    <th style="padding: 5px 10px; border: 1px solid #666;">Payment&#160;terms</th>
                    <th style="padding: 5px 10px; border: 1px solid #666;">Due&#160;date</th>
                   
                        <tr style="line-height: 25px; border: 1px solid #666;">
                            
                            <td style="padding: 5px 10px; border: 1px solid #666;">
                                ${invoiceDetails.invoiceData.invoiceNumber }
                            </td>
                      
                            <td style="padding: 5px 10px; border: 1px solid #666;">
                                ${invoiceDetails.invoiceData.invoiceDate[0]}/${invoiceDetails.invoiceData.invoiceDate[1]}/${invoiceDetails.invoiceData.invoiceDate[2] }
                            </td>
                        
                            
                            <td style="padding: 5px 10px; border: 1px solid #666;">
                                <g:if test="${invoiceDetails.invoiceData.paymentTerms == "0" || invoiceDetails.invoiceData.paymentTerms == null}">Due On Date</g:if>
                                <g:else>
                                    ${invoiceDetails.invoiceData.paymentTerms} Net </g:else>&#160;</td>
                       
                          
                            <td style="padding: 5px 10px; border: 1px solid #666;">
                                ${invoiceDetails.invoiceData.dueDate[0]}/${invoiceDetails.invoiceData.dueDate[1]}/${invoiceDetails.invoiceData.dueDate[2] }
                            </td>
                        
                           
                        </tr>
                    </tbody>
                </table>
            </div>
        <div style="width: 100%;">
            <div style="color: #000; font-size: 14px; margin-right: 0px; color: #666666;">
              
					
					<span style="float:left;width:350px;">
                <h2 style="font-size: 20px; margin: 5px 0px 0px 0px; color: #2d82c3; font-size: 15px; font-weight: bold; display: block;">From</h2>
                <p style="margin-top:5px">
                    <b style= "font-size: 15px;">
						${invoiceDetails.invoiceData.currentOffice.officeName }
					</b>
                    <br /> ${invoiceDetails.invoiceData.currentOffice.address}&#160;,&#160;${invoiceDetails.invoiceData.currentOffice.state}
                    ${invoiceDetails.invoiceData.currentOffice.pinCode }
                    <br /> ${invoiceDetails.invoiceData.currentOffice.country }
                    <br /> Phone:&#160;${invoiceDetails.invoiceData.currentOffice.officePhoneNo}
                    <br />
                    <a href="#" style="text-decoration: none;">
						${invoiceDetails.invoiceData.currentOffice.email}
					</a>
                </p>
                </span>
                
                 <span style="float:right;width:350px;">
                <h2 style="font-size: 20px; margin: 5px 0px 0px 0px; color: #2d82c3; font-size: 15px; font-weight: bold; display: block;">Send
					To</h2>
                <p style="display: block; margin: 5px 0 0px; font-size: 15px; font-weight: bold;">
                    ${invoiceDetails.invoiceData.clientName}
                </p>
                <p style="margin-bottom: 5px; margin-top: 5px;">
                    <g:if test="${invoiceDetails.invoiceData.clientContactPerson != " " && invoiceDetails.invoiceData.clientContactPerson != null && invoiceDetails.invoiceData.clientContactPerson != 'null'}">
                        ${invoiceDetails.invoiceData.clientContactPerson} - </g:if>
                    <g:if test="${invoiceDetails.invoiceData.invoiceSentTo != " " && invoiceDetails.invoiceData.invoiceSentTo != null}">
                        ${invoiceDetails.invoiceData.invoiceSentTo}
                    </g:if>
                </p>
                <span style="margin-bottom: 10px; display: block;"><g:if
						test="${invoiceDetails.invoiceData.clientContactAddress != "" && invoiceDetails.invoiceData.clientContactAddress != null && invoiceDetails.invoiceData.clientContactAddress != 'null'}">
						${invoiceDetails.invoiceData.clientContactAddress}
					</g:if></span>
					</span>
                
            </div>
            <%--<g:if test="${accountDetailsSelected==true}">
            --%><div style="max-width: 440px; color: #000; font-size: 14px; margin-right: 5px; color: #666666;float:left">
            <h2 style="font-size: 20px; margin:0; color: #2d82c3; font-size: 15px; font-weight: bold; display: block;">Payment Method</h2>
                
                   <table style="border:none;margin-top: 0px;padding-right:0px;margin-left:0px; width: 440px;">
                   	
                   	<tr><td>
                   
                   	<g:each in="${invoiceDetails.invoiceData.currentAccountDetailsDesList}">
                   	<p style="margin: 5px 5px 5px 0px;">${it}</p>
                   	</g:each>
                   	
                   	</td></tr>
                   	
                   	
                   	<%--<tr>
                   		<td><b>Account Number :</b>${invoiceDetails.invoiceData.currentBank.accountNumber }</td>
                   	</tr>
                   	<tr>
                   		<td><b>Account Holder Name :</b>${invoiceDetails.invoiceData.currentBank.accountHolderName}</td>
                   	</tr>
                   	<tr>
                   		<td><b>Bank Name :</b>${invoiceDetails.invoiceData.currentBank.bankName }</td>
                   	</tr>
                   	<tr>
                   		<td><b>IFSC Code :</b>${invoiceDetails.invoiceData.currentBank.ifscCode}</td>
                   	</tr>
                   	<tr>
                   		<td><b>Bank Address :</b>${invoiceDetails.invoiceData.currentBank.bankAddress}</td>
                   	</tr>
                   --%>
                   </table>
              
      		</div><%--
            </g:if>
            --%><div style="clear: both; width: 100%;"></div>
        </div>
        <div style="width: 83%; float: left; margin: 0px 0px; font-size: 14px; color: #666666;">

            <table style="border: 2px solid #666; margin: 20px 0 20px 0; padding: 0px; border-collapse: collapse; width: 100%;">
                <tbody>
                    <tr style="line-height: 25px; border: 1px solid #666; text-align: right; font-weight: bold; color: #2d82c3;">
                        <th style="padding: 5px 10px; border-bottom: 0.5px solid #666; text-align: left; width: 60%;">Description</th>
                        <th style="padding: 5px 10px; border-bottom: 0.5px solid #666; width: 13%;">Quantity</th>
                        <th style="padding: 5px 10px; border-bottom: 0.5px solid #666; width: 13%;">Unit&#160;price</th>
                        <th style="padding: 5px 10px; border-bottom: 0.5px solid #666; width: 13%;">Amount</th>
                    </tr>
                    <g:each in="${invoiceDetails.invoiceData.teamData}" var="member">
                        <tr style="line-height: 25px; border: 1px solid #666; text-align: right;">
                            <td style="padding: 5px 10px; border-bottom: 0.5px solid #666; text-align: left;">
                                ${member.name}
                                <g:if test="${member.description != " " && member.description != null}"> - ${member.description}
                                </g:if>
                            </td>
                            <td style="padding: 5px 10px; border-bottom: 0.5px solid #666;">
                                ${member.quantity}
                            </td>
                            <td style="padding: 5px 10px; border-bottom: 0.5px solid #666;">$ ${member.unitprice}
                            </td>
                            <td style="padding: 5px 10px; border-bottom: 0.5px solid #666;">$ ${member.totalamount}
                            </td>
                        </tr>
                    </g:each>
                    <tr style="line-height: 25px; border: 1px solid #666;">
                        <td colspan="4" style="padding: 0px;">
                            <table style="width: 100%; padding: 0px; border-collapse: collapse; border: 0px;">
                                <tbody>
                                    <tr style="line-height: 25px;">
                                       <td style="padding: 10px; width: 55%; border-right: 1px solid #666;" rowspan="4">
                                         <g:if test="${invoiceDetails.invoiceData.recipient!=null && invoiceDetails.invoiceData.recipient!='null' && invoiceDetails.invoiceData.recipient!=''}">
                                        <span style="width: 97%; display: block; color: #666666; margin-top: 0px; padding: 0px 2px; min-height: 75px; line-height: normal;">
												Note:
												${invoiceDetails.invoiceData.recipient}
										</span>
										 </g:if>
                                        </td>
                                       <td style="padding: 0px;">
                                            <table style="width: 100%; padding: 0px; border-collapse: collapse; border: 0px; text-align: right;">
                                                <tr style="line-height: 25px;">
                                                    <td style="padding: 5px 10px;">Subtotal</td>
                                                    <td style="padding: 5px 10px;">$ ${invoiceDetails.invoiceData.invoiceSubTotalAmount}</td>
                                                </tr>
                                                <g:if test="${invoiceDetails.invoiceData.discountAmount>0}">
                                                 <tr style="line-height: 25px;">
                                               		<td style="padding: 5px 10px;">Discount (${invoiceDetails.invoiceData.discountUnit }%)</td>
                                                    <td style="padding: 5px 10px;">&#45;$ ${invoiceDetails.invoiceData.discountAmount}</td>
                                                </tr>
                                                </g:if>
                                               
                                                <tr style="line-height: 25px; border-top: 2px solid #666; border-bottom: none;">
                                                    <td style="padding: 5px 10px; border-top: 1px solid #666;">Total</td>
                                                    <td style="padding: 5px 10px; border-top: 1px solid #666;">$ ${invoiceDetails.invoiceData.invoiceTotalAmount}
                                                    </td>
                                                </tr>
                                                <g:if test="${invoiceDetails.invoiceData.currencyType != 'USD'}">
                                                    <tr style="line-height: 25px;">
                                                        <td style="padding: 5px 10px;">Total Price in ${invoiceDetails.invoiceData.currencyType}</td>
                                                        <td style="padding: 5px 10px;"><asset:image src="${invoiceDetails.invoiceData.currencySymbolPng}" style="margin-top: -3px;" /> ${invoiceDetails.invoiceData.invoiceTotalAfterCurrencyChange}</td>

                                                    </tr>
                                                      <tr>
                                                <td style="padding: 5px 10px;">(Exchange Rate 1 $ = <asset:image src="${invoiceDetails.invoiceData.currencySymbolPng}" style="margin-top: -3px;" /> ${invoiceDetails.invoiceData.currencyRate})</td>
                                               
                                                </tr>
                                                </g:if>
                                            </table>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
		  </div>
        <div style="clear: both; width: 100%;"></div>
    </div>
</div>