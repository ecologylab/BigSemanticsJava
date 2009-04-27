/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
 * CONFIDENTIAL. Use is subject to license terms.
 */
package ecologylab.semantics.html.old;

import java.net.*;
import java.io.*;
import java.util.*;

import ecologylab.generic.*;
import ecologylab.net.ParsedURL;
import ecologylab.xml.XMLTools;


/**
 * State and functionality to parse an HTML page;
 * calls {@link Backend Backend} to perform actual productions on relevant
 * patterns.
 */
public class Parser extends Scan
{
   InputStream plainStream;
   
   /**
    * Indicates successful completion to outsiders who wonder.
    */
   boolean ready = false;

   // +++++++++++ parser statics +++++++++++ //
   static HashMap tagTable = new HashMap();

   static HashMap<Thread, HashMap<String, String>>	  attributesMapMap	= new HashMap<Thread, HashMap<String, String>>(5);

   // !!! change these 2 data structures together !!!
   static final String tags[] = 
   {
      "a", "/a", "img", "br", "p", "table", "tr", "ul", "dl",
      "body", "title", "/title", "meta", "frame",
      "script", "/script","style", "/style", "!--", "div", "/div", "/p", 
      "area", "iframe", "li", "/li", "select","/select","option", "/option",
      "i", "/i", "em", "/em", "b", "/b", "strong", "/strong",
      "/tr", "/table", "font"
   };
   // tval vals
   static final int A = 0, A_END = 1, IMG = 2, BR = 3, P = 4, 
				   TABLE = 5, TR = 6, UL = 7, DL = 8, BODY = 9, TITLE = 10,
				   TITLE_END = 11, META = 12, FRAME = 13, SCRIPT = 14,
				   SCRIPT_END = 15, STYLE = 16, STYLE_END = 17,
				   COMMENT = 18, DIV = 19, DIV_END = 20, P_END = 21, 
				   AREA = 22, IFRAME = 23,
				   LI=24, LI_END=25, 
				   SELECT=26, SELECT_END=27, 
				   OPTION=28, OPTION_END=29, 
				   ITALIC=30, ITALIC_END=31, EM = 32, EM_END = 33, 
				   BOLD = 34, BOLD_END = 35, STRONG = 36, STRONG_END = 37,
				   TR_END = 38, TABLE_END = 39, FONT = 40,
				   UNKNOWN_TAG = -1;

   static
   {
      for (int i = 0; i != tags.length; i++)
      {
		 tagTable.put(tags[i], new IntSlot(i));
      }
   }

   ////////////// Parser BackEnd //////////////
   Backend		backend;

   
   // --------------------- constructors and such ------------------------ //
   //
   public Parser(InputStream plainStream, Backend parent)
   {
      super(plainStream);
      this.plainStream	= plainStream;
      backend		= parent;
   }  

   // ------------------------ heart of the beast ------------------------- //
   //
   /**
    * Parse the page. Use the {@link Backend Backend} interface do
    * perform context-specific actions (productions on rules), such as
    (in CollageMachine) to add to collections.
    */
   public boolean parse() throws Exception
   {
	  if (show(3))
	  {
		  debug("start parsing!");
	  }
   	  
      boolean ok = false; // for returning a value

      int token;
      
      /**
       * boolean val for option tag
       */
      //debug(1, "parse(" + urlString+ ")");

      boolean optionOn = false;	

	  HashMap<String, String> attributesMap = getAttributesMap();
      try
      {
		 // outer token loop. parse until we recognize a tag
		 while ((token = scan()) != TT_EOF)
		 {
			boolean	 more	= true;
			
			// dispatch based on top-level token type
			switch (token)
			{
			//case TT_COMMENT:
			//mode		= OUTSIDE_TAG_MODE;
			//break;
			case TT_WORD:
			   if (sval.length() > 0)
			   {
				  if (optionOn)
				  {
					 //debug(5," \nOption on token = " + sval);
				  }
				  else
				  {
					  String textToken = XMLTools.unescapeXML(sval);
					  backend.newTextToken(textToken, " ");
				  }	  
			   }
			   break;
			case TT_WHITESPACE:
			   backend.newWhitespace();
			   break;
			case TT_TAG:
			   int	tag	= lookupTag();
			   
			   if (tag != OPTION)
			   {
				  optionOn	= false;
			   }

			   String	attr	= null;
			   //if (tag != -1)
			   //Debug.println("recognized tag = "+ tag);
			   if (tag != OPTION_END)
			   {
				  optionOn = false;
			   }

			   //int closeBlockStrength = Backend.WEAKER_END;

			   // attribute parsing loop
			   attributesMap.clear(); // get rid of any leftover attr/values

			   //mode	= INSIDE_TAG_MODE; // @TODO! not sure why needed

			   while (more && ((token = scan()) != TT_EOF))
			   {
				  switch (token)
				  {
					  case TT_ATTR:
						 attr	= sval;
						 break;
					  case TT_UNQUOTED_VAL:
					  case TT_QUOTED_VAL:
						 attributesMap.put(attr, sval);
						 if ("container".equals(attr))
						 {
							backend.setContainer(sval);
						 }
						 break;
						 // recognize end of tag 
					  case TT_TAG_CLOSE:
						 more	= false;
						 break;
				  }
			   } 
			   // end attribute parse loop
			   

			   // tag action dispatch switch
			   switch (tag)
			   {
			   case ITALIC:
			   case EM:
				  backend.setItalic(true);
				  break;
				  
			   case ITALIC_END:
			   case EM_END:			
				  backend.setItalic(false);
				  break;
				  
			   case BOLD:
			   case STRONG:
				  backend.setBold(true);
				  break;
				  
			   case BOLD_END:
			   case STRONG_END:
				  backend.setBold(false);
				  break;
				  
				  
			   case SCRIPT:
				  //debug("SCRIPT start -- ignoring");
				  ignoreUntilTag("/script");
				  //debug("SCRIPT end");
				  break;
			   case STYLE:
				   //debug(3, "STYLE start -- ignoring");
				  ignoreUntilTag("/style");
				  break;
			   case TITLE:
				  backend.startTitle();
				  break;
			   case TITLE_END:
				  backend.closeTitle();
				  break;
				  
			   case DIV:
				  optionOn = false;
				  backend.closeBlock(tags[tag], attributesMap);
				  break;

			   case FONT:
			   	  optionOn = false;
			   	  backend.fontTag(attributesMap);
			   	  break;
			   case P:
				  optionOn = false;
				  backend.closeBlock(tags[tag], attributesMap);
				  
				  break;
			   case BR:
				  optionOn = false;
				  backend.closeBlock(tags[tag], attributesMap);
				  //backend.closeBlock(closeBlockStrength);
				  break;
			   case P_END:
			   case TR:   		// <table> is always followed by <tr>
			   case TR_END:  	// <table> is always followed by <tr>
			   case TABLE:
			   case TABLE_END:
		
			   case DIV_END:
			   case LI:
			   case LI_END:
			   case UL:
			   case DL:
			   case SELECT_END:
				  optionOn = false;
				  backend.closeBlock(tags[tag], attributesMap);
				  //backend.closeBlock(closeBlockStrength);
				  break;

			   case A_END: // end of link
				  backend.closeHref();
				  break;

			   case OPTION:
				  optionOn = true;

				  String value = attributesMap.get("value");
				  if ((value != null) && (value.startsWith("http://") || 
					  value.endsWith(".html") || value.endsWith(".htm")))
				  {
					 backend.newMinedHref(value);
				  }
				  break;
			   case OPTION_END:
				  break; 		
			   case BODY:
				  backend.newBody(attributesMap);
				  break;
			   case A:
				  backend.newAHref(attributesMap);
				  break;
			   case AREA:		   // element of image map
				  backend.newImageMapArea(attributesMap);
				  break;
			   case FRAME:
				  backend.newFrame(attributesMap);
				  break;
			   case IFRAME:
				  backend.newIFrame(attributesMap);
				  break;
			   case IMG:
				  backend.newImg(attributesMap);
				  break;
			   case META:
				  backend.meta(attributesMap);
				  break;
			   default:
				  //Debug.println("Parser uninteresting tag ="+
				  //token + " mode=" + mode);
				  break;
			   }				   // end tag action dispatch switch
			   break;			   // end case TT_TAG:
			default:			   // unrecognized token type
			   break;
			}			// end dispatch switch on top-level token type
		 }		   // end outer token loop. parse until we recognize a tag

		 // clean up. ??? could change to closeBody()
		 // in case there's content to flush
		 backend.closeBlock("br", attributesMap);
		 // backend.closeBlock(Backend.WEAKER_END);
		 //	Debug.println("Finished parsing " + urlString);
		 ok	= true;
      } catch (Exception e)
      {
    	  //e.printStackTrace();
     	  throw e;
      }
      
      finally
      {
		 ready = true;
		 try
		 {
			// bufferedStream.close();
			bufferedReader.close();
			// Debug.println("Closed " + bufferedStream);
		 } catch (IOException e)
		 {
			debug("Error closing " + bufferedReader);
		 }
		 stringBuffersPool.release(buffer);	
		 attributesMap.clear();
		 buffer = null;
      }
      return ok;
   }

   /**
    * Interpret the HTML tag found in sval (after the <).
    * Return result in tval.
    */
   int lookupTag()
   {
      int tval;
      // identify comment start and end w no whitespace in the midst
      // as a special case
      if (sval.startsWith("!--"))
      {
		 if (sval.endsWith("-->"))
		 {
			debug("lookupTag() TT_END_COMMENT "+sval);
			tval	= TT_END_COMMENT;
		 }
		 else
		 {
			mode	= COMMENT_MODE;	// set scanner mode
			tval	= COMMENT;	// (from tag table)
			debug("lookupTag() COMMENT_MODE " +sval);
		 }
      }
      else
      {
		 IntSlot slot	= (IntSlot) tagTable.get(sval);
		 if (slot != null)
		 {
			tval	= slot.value;
		 }
		 else
		 {
			tval	= UNKNOWN_TAG;  // (from tag table)
		 }
		 // debug(4, "lookupTag(" + sval + ") = " + tval);
      }
      //println(sval +" "+tval);
      return tval;
   }

   /**
    * This hack throws away mining JavaScript for now.
    * 
    * @param trigger
    * @throws IOException
    */
   void ignoreUntilTag(String trigger)
   throws IOException
   {
	   //println("ignoreUntilTag(" + trigger);
	   int index	= 0;
	   int match	= trigger.charAt(index++);
	   int ic		= TT_EOF;
	   do
	   {
		   ic= bufferedReader.read();
		   if (ic == match)
		   {
			   if (index == trigger.length())
				   break;
			   // else
			   match	= trigger.charAt(index++);
		   }
		   else if (index != 1)
		   {
			   
			   index	= 0;
			   match	= trigger.charAt(index++);
		   }
		   
	   } while (ic != TT_EOF);
	   
   }
   void ignoreUntilTagOld(String trigger)
      throws IOException
      {
	   	 int token;
		 while ((token = scan()) != TT_EOF)
		 {
			println(sval);
			if (sval != null)
			{
			   int htm	= sval.indexOf(".htm");
			   int jpg	= sval.indexOf(".jpg");
			   
			   if( sval.startsWith("//"))
			   {
					mode = SCRIPT_COMMENT_MODE;
			   }
			   
			   if (htm != -1)
			   {
				  if (htm == 0)
				  {
					  // toss plain old .htm
					  break;
				  }
				  
				  //debug("URL found amidst JavaScript: " + sval);
				  int html	= sval.indexOf(".html");
				  int end;
				  
				  if (html != -1)
				  {
					 end	= html + 5;
				  }
				  else
				  {
					 end	= htm + 4;
				  }
				  
				  sval	= sval.substring(0,end);
				  int singleQ	= sval.lastIndexOf('\'');
				  int doubleQ	= sval.lastIndexOf('"');
				  int start	= (singleQ > doubleQ) ? singleQ : doubleQ;
				  start++;
				  sval	= sval.substring(start);
				  //debug("URL using stuff amidst JavaScript: " + sval);
				  backend.newMinedHref(sval);
			   }
			   else if (jpg != -1)
			   {
				  sval	= sval.substring(0,jpg+4);
				  int singleQ	= sval.lastIndexOf('\'');
				  int doubleQ	= sval.lastIndexOf('"');
				  int start	= (singleQ > doubleQ) ? singleQ : doubleQ;
				  start++;
				  sval	= sval.substring(start);
				  backend.newMinedImg(sval);

			   }
			   else if ((token == TT_TAG) && (sval.equals(trigger)))
			   {
				  break;
			   }
			}
			//ignoreMode = false;
		 }
		 //debug(3, "end of ignore: " + trigger);
      }
	  // ------------------------- utilities ------------------------------ //
	  //
	  // find an instance of toMatch somewhere in in (case insensitive)
	/**
	 * indicates that the parse is complete
	 */
	public boolean isReady() { return ready; }

	public synchronized void waitForReady()
	{
		//Debug.println("Parser.waitForReady(" + string + ")");
		if (!ready)
		{
			try { wait(); } catch (InterruptedException e) { }
		}
		//	Debug.println("Parser.waitForReady(" + string + ") isReady");
   }

   static final String STUFF0 =
       "<div class=\"cnnMainT1Hd\"><h2><a href=\"http://www.cnn.com/2004/ALLPOLITICS/07/06/kerry.vp/index.html\" style=\"color: rgb(0, 0, 0);\">Edwards takes VP role</a></h2></div><div style=\"background-color: rgb(255, 255, 255);\"><img container=\"http://www.cnn.com/\" src=\"http://i.cnn.net/cnn/images/1.gif\" alt=\"\" width=\"1\" height=\"10\"></div>    <a href=\"http://www.cnn.com/2004/ALLPOLITICS/07/06/kerry.vp/index.html\"><img container=\"http://www.cnn.com/\" src=\"http://i.cnn.net/cnn/2004/ALLPOLITICS/07/06/kerry.vp/top.main.edwards.crowd.pool.jpg\" width=\"280\" height=\"210\" alt=\"Edwards takes VP role\" border=\"0\" hspace=\"0\" vspace=\"0\"></a><div class=\"cnnMainT1\">";
    
   static final String STUFF =
       "<td width=\"184\" align=\"left\" valign=\"top\"><!--NYT Logo --><a href=\"http://www.nytimes.com/\"><IMG src=\"http://graphics8.nytimes.com/images/article/header/nytlogoleft_article.gif\" width=\"184\" height=\"40\" align=\"left\" border=\"0\" alt=\"The New York Times\"></a></td>\n<script type=\"text/JavaScript\">\n<!--\n	\n	function goToURL(obj){\n		var f = (obj.section) ? obj : obj.form;\n		var selected = f.section.selectedIndex;\n		var URL = f.section.options[selected].value;\n		if (URL != \"\") document.location = URL;\n		return false;\n	}\n\n	function setForumsAction(obj){\n		var t = (obj.search) ? obj : obj.form;\n		var optval = t.search.selectedIndex;\n\n		if (optval == \"2\"){\n			 document.forumsform.action = \"http://query.nytimes.com/search/query\";\n		} else{\n			document.forumsform.action = \"\";\n\n		}\n	}\n//-->\n</script>\n";
    
    
   static final String STUFF2 =
	"\n<br clear=\"all\">\n<!-- eLibrary module: wash article -->\n<script>\nvar numKeywords = 3;        // user defined -> max num of bullets in list\nvar strKeywords = \"suggested%5fpolitics;suggested%5fcontinuous;\";\nvar clickGoto = \"http://www.nytimes.com/adx/bin/adx_click.html?type=goto&page=www.nytimes.com/yr/mo/day/politics&pos=Middle5&camp=elibrary20-nyt9&ad=elibrary_ver5b_wash_form&goto\";\nvar refid = \"nyt_washington1\";\n</script>\n<script language=\"JavaScript\" src=\"http://www.nytimes.com/ads/elibrary/MC_elibrary_1.03b.js\" type=\"text/javascript\"></script>\n<!-- /eLibrary module: wash article -->\n<br clear=\"all\"><br>\n<!-- ANIMATED MARKETING MODULE STARTS HERE -->\n\n	<!-- MAKE SURE THIS SCRIPT IS INCLUDED IN CUT AND PASTE! -->\n	  \n 	<SCRIPT LANGUAGE=\"JavaScript\" TYPE=\"text/javascript\">\n		<!--\n			function goModuleLocation( thisFormSelect ) {\n  			var thisSelection = thisFormSelect.options[thisFormSelect.selectedIndex].value;\n  			if (thisSelection != \"\") {\n  			// location = thisSelection; \n			var newWin = window.open (thisSelection,'','');\n  			}\n		}\n		//-->\n	</SCRIPT>\n\n	<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n	<tr>\n		<td align=\"left\" valign=\"top\" colspan=\"3\" width=\"100%\">\n		<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n			<tr>\n				<td align=\"left\" valign=\"top\"><img src=\"http://graphics8.nytimes.com/marketing/2005module/images/mm_01.gif\" width=\"14\" height=\"55\" alt=\"\" /></td>			\n				<td align=\"center\" valign=\"top\" background=\"http://graphics8.nytimes.com/marketing/2005module/images/mm_02.gif\" width=\"100%\"> \n			 	<a href=\"http://www.nytimes.com/adx/bin/adx_click.html?type=goto&page=www.nytimes.com/yr/mo/day/politics&pos=Right&camp=nytnyt-marketingmoduleTHUR&ad=marketingmodTHUR_3.html&goto=http://www.nytimes.com/pages/automobiles/index.html\" target=\"_blank\"><img src=\"http://graphics8.nytimes.com/marketing/2005module/images/h_automobiles.gif\" alt=\"Automobiles\" width=\"130\" height=\"36\" border=\"0\"></a><br>\n		 		<a href=\"http://www.nytimes.com/adx/bin/adx_click.html?type=goto&page=www.nytimes.com/yr/mo/day/politics&pos=Right&camp=nytnyt-marketingmoduleTHUR&ad=marketingmodTHUR_3.html&goto=http://www.nytimes.com/pages/automobiles/index.html\" target=\"_blank\" style=\"font-size:11px; font-family: arial, helvetica, sans-serif; font-weight: bold; color:000066;\">nytimes.com/autos</a></td>\n				<td align=\"left\" valign=\"top\"><img src=\"http://graphics8.nytimes.com/marketing/2005module/images/mm_03.gif\" width=\"14\" height=\"55\" alt=\"\" /></td>\n			</tr>\n		</table>\n		</td>\n	</tr>\n	<tr>\n		<td align=\"left\" valign=\"top\" background=\"http://graphics8.nytimes.com/marketing/2005module/images/mm_10.gif\" width=\"4\"></td>\n		<td align=\"left\" valign=\"top\" width=\"100%\"><img src=\"http://graphics8.nytimes.com/images/misc/spacer.gif\" width=\"4\" height=\"10\"></td>\n		<td align=\"right\" valign=\"top\" background=\"http://graphics8.nytimes.com/marketing/2005module/images/mm_09.gif\"></td>\n	</tr>\n	<tr>\n";

   static final String G_STUFF	=
	   "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><title>&quot;hurricane katrina&quot; - Google Search</title><style><!--\ndiv,td{color:#000}\n.f{color:#666}\n.flc,.fl:link,.ft a:link,.ft a:visited,.ft a:hover,.ft a:active{color:#77c}\na:link,.w,a.w:link,.w a:link,.q:visited,.q:link,.q:active,.q{color:#00c}\na:visited,.fl:visited{color:#551a8b}\na:active,.fl:active{color:red}\n.t{background:url(images/grad.png) repeat-x bottom;border:1px solid #ebebeb;border-bottom:1px solid #c7c7c7;color:#000;padding:5px 8px 8px}\n.j{width:34em}\n.h{color:#36c}\n.i,.i:link{color:#a90a08}\n.a,.a:link{color:green}\n.z{display:none}\ndiv.n{margin-top:1ex}\n.n a{font-size:10pt;color:#000}\n.n .i{font-size:10pt;font-weight:bold}\n.b a{font-size:12pt;color:#00c;font-weight:bold}\n#np,#nn,.nr,#logo span,.ch{cursor:pointer;cursor:hand}\n.tas{padding:3px 3px 3px 5px}\n.taf{padding:3px 3px 6px 5px}\n.tam{padding:6px 3px 6px 5px}\n.tal{padding:6px 3px 3px 5px}\n#gbar{float:left;height:22px}\n#gbarl{border-top:1px solid #c9d7f1;font-size:0;height:1px;position:absolute;right:0;top:24px;width:110%}\n#gbar a{color:#00c}\n#gbar .gbard{background:#fff;border:1px solid;border-color:#c9d7f1 #36c #36c #a2bae7;display:none;font-size:13px;position:absolute;top:24px;z-index:1000}\n#gbar .gbard a{display:block;padding:.2em .5em;text-decoration:none;white-space:nowrap}\n#gbar .gbard a:hover{background:#36c;color:#fff}\n#gbar td{font-size:13px;padding-right:1em}\n#guser{font-size:13px;padding-bottom:7px !important;padding-top:0}\n#gbarc{font-size:0;height:1px}\n.sl,.r{font-weight:normal;margin:0;display:inline}\n.sl{font-size:84%}\n.r{font-size:1em}\n.e{margin:.75em 0}\n.mblink:visited{color:#00c}\n.m{color:#666;font-size:84%}\n.sm{display:block;margin:0;margin-left:40px}\n.bl{display:none}\n.fl2,.fl2:link,.fl2:visited{color:#77c}\n.fl2:active{color:red}\n#navbar div,#logo span{background:url(/images/nav_logo.png) no-repeat;overflow:hidden;height:26px}\n#navbar .nr{background-position:-60px 0;width:16px}\n#navbar #np{width:44px}\n#navbar #nf{background-position:-26px 0;width:18px}\n#navbar #nc{background-position:-44px 0;width:16px}\n#navbar #nn{background-position:-76px 0;width:66px;margin-right:34px}\n#navbar #nl{background-position:-76px 0;width:46px}\n#logo{display:block;width:150px;height:52px;position:relative;overflow:hidden;margin:15px 0 12px}\n#logo span{background-position:0 -26px;position:absolute;top:0;left:0;width:100%;height:100%}\nbody,td,div,.p,a{font-family:arial,sans-serif}\n.g{margin:1em 0}\n#sd{font-size:84%;font-weight:bold}\n#ap{font-size:64%}\n--></style>\n<script><!--\n(function(){window.google={kEI:\"RedlRqOKE4eajgHDyNGQBQ\",kEXPI:\"17259,17291,17469,52142\",kHL:\"en\"};})();(function(){window.ss=function(){window.status=\"\";return true};})();(function(){if(document.images){new Image().src=\"/url?sa=Q&q=%22hurricane+katrina%22&num=15&ct=q&ei=RedlRqOKE4eajgHDyNGQBQ&sig2=Vcd8X_MzzHTQwqqaoKKUug\";}\n})();(function(){window.rwt=function(b,d,f,j,k,g,l){var a=window.encodeURIComponent?encodeURIComponent:escape,h=\"\",i=\"\",c=b.href.split(\"#\"),e=\"\";if(d){h=\"&oi=\"+a(d)}if(f){i=\"&cad=\"+a(f)}if(g){e=\"&usg=\"+g}b.href=\"/url?sa=t\"+h+i+\"&ct=\"+a(j)+\"&cd=\"+a(k)+\"&url=\"+a(c[0]).replace(/\\+/g,\"%2B\")+\"&ei=RedlRqOKE4eajgHDyNGQBQ\"+e+l+(c[1]?\"#\"+c[1]:\"\");b.onmousedown=\"\";return true};})();window.gbar={};(function(){function o(a,b,c){var e=\"on\"+b;if(a.addEventListener){a.addEventListener(b,c,false)}else if(a.attachEvent){a.attachEvent(e,c)}else{var d=a[e];a[e]=function(){var f=d.apply(this,arguments),g=c.apply(this,arguments);return f==undefined?g:(g==undefined?f:g&&f)}}};var h=window.gbar,m=[\"affdom\",\"channel\",\"client\",\"hl\",\"hs\",\"ie\",\"lr\",\"ned\",\"oe\",\"og\",\"rls\",\"rlz\"];function i(a){return escape(unescape(a.replace(/\\+/g,\" \"))).replace(/\\+/g,\"%2B\")}function j(a){return a==\"c\"||a==\"o\"||a==\"m\"}h.getHtml=function(a){var b;for(var c=0;c<a.length;c++){if(a[c][2]==\"\"){b=a[c][0]}}var e=j(b)?\" target=_blank\":\"\",d=\"<td nowrap>\",f=\"<table border=0 cellpadding=0 cellspacing=0 style=margin-left:\"+h.getPad(true)+\"px><tr>\"+d;for(var c=0;c<a.length;c++){if(a[c][0]==b){f+=a[c][1].bold()+d}else{f+=\"<a href=\";if(a[c][3]==3){f+='# onclick=\"this.blur();return gbar.toggle(event)\" style=text-decoration:none'+e+\"><u>\"+a[c][1]+\"</u> <span style=font-size:11px>&#9660;</span></a><tr><td colspan=\"+c+\"><td><iframe class=gbard id=gbarif style=border:0;z-index:999></iframe><div class=gbard id=gbardd onclick=gbar.stopB(event)>\";d=\"\"}else{f+=n(b,a[c][0],a[c][2])+e+\" onclick=gbar.close(event)>\"+a[c][1]+\"</a>\"+d}}}f+=\"</div></table>\";return f};h.getPad=function(a){var b=-1,c=a?10:4,e=document.body.currentStyle,d=document.defaultView;if(e){b=a?e.marginLeft:e.marginTop}else if(d){b=a?d.getComputedStyle(document.body,\"\").marginLeft:d.getComputedStyle(document.body,\"\").marginTop}b=parseInt(b,10);return b>=0&&b<c?c-b:1};function n(a,b,c){var e=window.location.search.substring(1),d=e.match(\"q=([^&]*)\"),f=e.match(\"near=([^&]*)\"),g=c+(c.match(\"[?]\")?\"&\":\"?\");g+=\"tab=\"+a+b;if(j(b)&&window.location.protocol==\"https:\"){g=g.replace(\"http:\",\"https:\")}if(!j(b)&&!j(a)){for(var k=0;k<m.length;k++){var l=e.match(\"(\"+m[k]+\")=([^&]*)\");if(l){g+=\"&\"+l[1]+\"=\"+i(l[2])}}if(d&&f&&a==\"l\"&&b!=\"l\"){g+=\"&q=\"+i(d[1])+\"+\"+i(f[0])}else if(d){g+=\"&q=\"+i(d[1])}}return g}h.toggle=function(a){h.stopB(a);var b=document.getElementById(\"gbardd\"),c=document.getElementById(\"gbarif\");if(b&&c){b.style.display=b.style.display==\"block\"?\"none\":\"block\";c.width=b.offsetWidth;c.height=b.offsetHeight;c.style.display=b.style.display}return false};h.close=function(a){var b=document.getElementById(\"gbardd\");if(b&&b.style.display==\"block\"){h.toggle(a)}};h.stopB=function(a){if(!a){a=window.event}a.cancelBubble=true};o(document,\"click\",h.close);})();//-->\n</script></head><body bgcolor=#ffffff onload=\"window.ManyBox && ManyBox.init();window.MultiHistory && MultiHistory.initialize();if(document.images){new Image().src='/images/nav_logo3.png'}\" topmargin=3 marginheight=3><noscript></noscript><div id=gbarl></div><div height=1 id=gbarc width=100%><img alt='' height=1 id=gbari width=1></div><div id=gbar></div><script>document.getElementById('gbarc').style.height=(document.getElementById('gbari').height=window.gbar.getPad())+'px';document.getElementById('gbar').innerHTML=window.gbar.getHtml([['w','Web','',1],['i','Images','http://images.google.com/images?um=1',1],['v','Video','http://video.google.com/videosearch?um=1',1],['n','News','http://news.google.com/news?um=1',1],['l','Maps','http://maps.google.com/maps?um=1',1],['m','Gmail','http://mail.google.com/mail?um=1',1],['','more','#',3],['b','Blog Search','http://blogsearch.google.com/blogsearch?um=1',2],['j','Blogger','http://www.blogger.com/?um=1',2],['p','Books','http://books.google.com/books?um=1',2],['c','Calendar','http://www.google.com/calendar?um=1',2],['o','Documents','http://docs.google.com/?um=1',2],['e','Finance','http://finance.google.com/finance?um=1',2],['g','Groups','http://groups.google.com/groups?um=1',2],['z','Labs','http://labs.google.com/',2],['0','Orkut','http://www.orkut.com/?um=1',2],['t','Patents','http://www.google.com/patents?um=1',2],['q','Photos','http://picasaweb.google.com/lh/searchbrowse?um=1&psc=G',2],['f','Products','http://www.google.com/products?um=1',2],['y','Reader','http://www.google.com/reader?um=1',2],['s','Scholar','http://scholar.google.com/scholar?um=1',2]]);</script><noscript><div id=gbarl></div><div id=gbar><table style=margin-left:2px border=0 cellpadding=0 cellspacing=0><tr><td><a href=/webhp>Web</a><td><a href=/imghp>Images</a><td><a href=http://video.google.com/>Video</a><td><a href=/nwshp>News</a><td><a href=/maphp>Maps</a><td><a href=http://mail.google.com/?ui=html>Mail</a><td><a href=/options style=text-decoration:none><u>more</u> &raquo;</a></table></div></noscript><div align=right id=guser style=\"font-size:84%;padding-bottom:4px\" width=100%><nobr><b>andruid.filter@gmail.com</b>&nbsp;|&nbsp;<a href=\"javascript:gnb._open()\">My Notebooks</a>&nbsp;|&nbsp;<a href=\"http://www.google.com/searchhistory/?hl=en\">Web History</a>&nbsp;|&nbsp;<a href=\"https://www.google.com/accounts/ManageAccount\">My Account</a>&nbsp;|&nbsp;<a href=\"http://www.google.com/accounts/Logout?continue=http://www.google.com/search%3Fq%3D%2522hurricane%2Bkatrina%2522%26num%3D15\">Sign out</a></nobr></div><table border=0 cellpadding=0 cellspacing=0 width=100% style=clear:left><tr><form name=gs method=GET action=/search><td valign=top><a id=logo href=\"http://www.google.com/webhp?hl=en\" title=\"Go to Google Home\">Google<span></span></a></td><td>&nbsp;&nbsp;</td><td valign=top width=100% style=\"padding-top:0px\"><table cellpadding=0 cellspacing=0 border=0><tr><td style=\"padding-top:24px\"><img align=right alt=\"\" height=1 width=1></td></tr><tr><td><table border=0 cellpadding=0 cellspacing=0><tr><td nowrap><input type=hidden name=num value=15><input type=hidden name=hl value=\"en\"><input type=text name=q size=41 maxlength=2048 value=\"&quot;hurricane katrina&quot;\" title=\"Search\"><font size=-1> <input type=submit name=\"btnG\" value=\"Search\"><span id=hf></span></font></td><td nowrap><span id=ap>&nbsp;&nbsp;<a href=/advanced_search?q=%22hurricane+katrina%22&num=15&hl=en>Advanced Search</a><br>&nbsp;&nbsp;<a href=/preferences?q=%22hurricane+katrina%22&hl=en>Preferences</a></span></td></tr></table></td></tr></table><table cellpadding=0 cellspacing=0 border=0><tr><td><font size=-1> </font></td></tr><tr><td height=7><img width=1 height=1 alt=\"\"></td></tr></table></td></form></tr></table><script src=\"/extern_js/f/CgJlbhICdXMYBw/ErQnJTrFThw.js\"></script><table border=0 cellpadding=0 cellspacing=0 width=100% class=\"t bt\"><tr><td nowrap><span id=sd>&nbsp;Web&nbsp;</span><font size=-1>&nbsp;&nbsp;&nbsp;<a class=q href=\"http://news.google.com/news?q=%22hurricane+katrina%22&num=15&ie=UTF-8&oe=UTF-8&um=1&sa=N&tab=wn&oi=revisions_inline&resnum=0&ct=property-revision&cd=1\">News</a>&nbsp;&nbsp;&nbsp;<a class=q href=\"http://images.google.com/images?q=%22hurricane+katrina%22&num=15&ie=UTF-8&oe=UTF-8&um=1&sa=N&tab=wi&oi=revisions_inline&resnum=0&ct=property-revision&cd=2\">Images</a>&nbsp;&nbsp;&nbsp;</font></td><td align=right nowrap><font size=-1>Results <b>1</b> - <b>15</b> of about <b>5,760,000</b> for <b>&quot;<a href=\"/url?sa=X&amp;oi=dict&amp;ei=RedlRqOKE4eajgHDyNGQBQ&amp;sig2=qK2He4JoPZovi0cQz7HemQ&amp;q=http://www.answers.com/hurricane%26r%3D67&amp;usg=AFQjCNGs5Yrl9EvVLhMxeU7yp2XVe5jARQ\" title=\"Look up definition of hurricane\">hurricane</a> katrina&quot;</b>.  (<b>0.05</b> seconds)&nbsp;</font></td></tr></table><table cellspacing=0 cellpadding=0 width=25% align=right id=mbEnd bgcolor=#ffffff border=0 class=ra><tr><td colspan=4><font size=-1>&nbsp;</font></td></tr><tr><td rowspan=5 >&nbsp;&nbsp;</td><td width=1 bgcolor=#c7c7c7 rowspan=5><img width=1 height=1 alt=\"\"></td><td rowspan=5 >&nbsp;&nbsp;</td><td height=25 align=center><h2 class=\"sl f\">Sponsored Links</h2></td></tr><tr height=7><td><img width=1 height=1 alt=\"\"></td></tr><tr><td nowrap onmouseover=\"return ss()\"><font size=-1>  <font size=+0><a id=an1 href=/url?sa=L&ai=B7PrDRedlRvzpE6WciwGb9pneBZ3P6BTF0aWUAumr4PQHoKQoEAEYASgIOAFQ0Yq_sPz_____AWDJluyM5KTIE5gBi4cBqgEJMk5SUysyR01MyAEB2QM0ouxVrLNTgA&num=1&q=http://www.lungusa.org/site/pp.asp%3Fc%3DdvLUK9O0E%26b%3D1024591&usg=AFQjCNGq0ohNVl-eg3VyKewLnJQ0Xi2LVg><b>Hurricane Katrina</b></a></font><br>Recovery Resources for Those<br>Affected by <b>Hurricane Katrina</b><br><span class=a>www.lungusa.org</span><br>    <br><font size=+0><a id=an2 href=/url?sa=L&ai=B0c-LRedlRvzpE6WciwGb9pneBabj6iOGqvCDA6bx4JkIwJaxAhACGAIoCDgBULuA1jRgyZbsjOSkyBOqAQkyTlJTKzJHTUzIAQHZAzSi7FWss1OA4AMA&num=2&q=http://www.democratsenators.org/dia/organizations/marylandrieu/campaign.jsp%3Fcampaign_KEY%3D9%26track%3Dfair_google&usg=AFQjCNGwAID7AGIuIrvMiixO5GyT4j8qPA>Rebuild the Gulf Coast</a></font><br>Co-sponsor Mary Landrieu&#39;s bill to<br>aid <b>Hurricane Katrina</b>/Rita victims<br><span class=a>www.DemocratSenators.org</span><br>    <br><font size=+0><a id=an3 href=/pagead/iclk?sa=l&ai=BTLFgRedlRvzpE6WciwGb9pneBfCn3xz87MSYAvCyi44GoOg7EAMYAygIOAFQ4YGphQVgyZbsjOSkyBOYAaefBqABgIT2_AOqAQkyTlJTKzJHTUzIAQGAAgHZAzSi7FWss1OA&num=3&adurl=http://www.urban.org/afterkatrina/index.cfm>After <b>Hurricane Katrina</b></a></font><br>Nonpartisan policy options to<br>consider for rebuilding New Orleans<br><span class=a>www.Urban.org</span><br>    <br><font size=+0><a id=an4 href=/url?sa=L&ai=BKJwDRedlRvzpE6WciwGb9pneBdCYvRHs_6jLAejv1foJkKk2EAQYBCgIOAFQt9HpxgJgyZbsjOSkyBOYAeiHAZgBp58GqgEJMk5SUysyR01MyAEBgAIB2QM0ouxVrLNTgA&num=4&q=http://www.hurricanearchive.org&usg=AFQjCNHuahMJ0TDjZ71dE06iTA1ymTYGkw>Share a Hurricane Photo</a></font><br>Tell us how Katrina affected you<br>Visit Hurricane Digital Memory Bank<br><span class=a>www.HurricaneArchive.org</span><br>    <br><font size=+0><a id=an5 href=/url?sa=L&ai=ByJMVRedlRvzpE6WciwGb9pneBa-80Qvthc3uAZPOoYAIgOowEAUYBSgIOAFQk_2HtANgyZbsjOSkyBOqAQkyTlJTKzJHTUzIAQHZAzSi7FWss1OA&num=5&q=http://forums.sunherald.com/n/mb/listsf.asp%3Fwebtag%3Dkr-biloxkatrina&usg=AFQjCNGlXVKLmyKWMKWIMflUranFarA5jg>Find Hurricane Survivors</a></font><br>Looking for lost family? Check this<br>Biloxi Message Board for news.<br><span class=a>www.SunHerald.com</span><br>    <br><font size=+0><a id=an6 href=/pagead/iclk?sa=l&ai=BlEfBRedlRvzpE6WciwGb9pneBeK15iummrrCAci04fgF4IkcEAYYBigIOAFQiqmmY2DJluyM5KTIE6AB7_WZ_wOqAQkyTlJTKzJHTUzIAQGAAgHZAzSi7FWss1OA&num=6&adurl=http://www.nrdc.org/legislation/katrina/katrinainx.asp>What Katrina Revealed</a></font><br>NRDC’s plan for a sustainable<br>future on the Gulf Coast.<br><span class=a>www.NRDC.org</span><br>    <br><font size=+0><a id=an7 href=/pagead/iclk?sa=l&ai=BnJohRedlRvzpE6WciwGb9pneBfuyvAebzZSiAe-By3ew4y0QBxgHKAg4AVDu5obmBGDJluyM5KTIE5gBlKAGoAH9j-T9A6oBCTJOUlMrMkdNTMgBAdkDNKLsVayzU4A&num=7&adurl=http://www.ri.org/countries.php%3Fcid%3D15>Remember the Hurricane?</a></font><br>The people of Louisiana still do.<br>Help make a long-term difference<br><span class=a>www.ri.org</span><br>    <br><font size=+0><a id=an8 href=/url?sa=L&ai=BOTSDRedlRvzpE6WciwGb9pneBZzM2xrY2OTtAqjNz6UGkNYnEAgYCCgIOAFQmci36Pr_____AWDJluyM5KTIE5gBi4cBqgEJMk5SUysyR01MyAEB2QM0ouxVrLNTgA&num=8&q=http://www.emergencycommunities.org&usg=AFQjCNHNP27dGOpG6i3HcCi3pA42HI44cQ><b>Hurricane Katrina</b> Support</a></font><br>Help Victims of Katrina Today! Get<br>More Info on Ways to Volunteer.<br><span class=a>www.emergencycommunities.org</span><br>  </font></td></tr><tr height=7><td><img width=1 height=1 alt=\"\"></td></tr><tr><td height=25 align=center><font size=-1></font></td></tr></table><div id=res>    <!--a--><div><div class=g><!--m--><link rel=\"prefetch\" href=\"http://en.wikipedia.org/wiki/Hurricane_Katrina\"><h2 class=r><a href=\"http://en.wikipedia.org/wiki/Hurricane_Katrina\" class=l onmousedown=\"return rwt(this,'','','res','1','AFQjCNFy6yl_T6NjORsLBFqImVIFeOXKsg','&amp;sig2=GgYzc_C8wUIne4tzwFoAeg')\"><b>Hurricane Katrina</b> - Wikipedia, the free encyclopedia</a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>Contains history of the storm, and its effects on the region.<br><span class=a>en.wikipedia.org/wiki/Hurricane_Katrina - 265k - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:ocI2DXaYpq8J:en.wikipedia.org/wiki/Hurricane_Katrina+%22hurricane+katrina%22&hl=en&ct=clnk&cd=1&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:en.wikipedia.org/wiki/Hurricane_Katrina\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://en.wikipedia.org/wiki/Hurricane_Katrina')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g style=\"margin-left:2.5em\"><!--m--><h2 class=r><a href=\"http://en.wikipedia.org/wiki/Effect_of_Hurricane_Katrina_on_New_Orleans\" class=l onmousedown=\"return rwt(this,'','','res','2','AFQjCNGLZwtlJeFgFFEfEPWg1z9blv0JTw','&amp;sig2=Pe-LXU0j5PdBIoOGw-GIXw')\">Effect of <b>Hurricane Katrina</b> on New Orleans - Wikipedia, the free <b>...</b></a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j hc\"><font size=-1>The effect of <b>Hurricane Katrina</b> on New Orleans was catastrophic and long-lasting. As Katrina passed east of New Orleans on August 29, 2005, winds were in <b>...</b><br><span class=a>en.wikipedia.org/wiki/Effect_<wbr>of_Hurricane_Katrina_on_New_Orleans - 117k - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:DbeD7WSvXyUJ:en.wikipedia.org/wiki/Effect_of_Hurricane_Katrina_on_New_Orleans+%22hurricane+katrina%22&hl=en&ct=clnk&cd=2&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:en.wikipedia.org/wiki/Effect_of_Hurricane_Katrina_on_New_Orleans\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://en.wikipedia.org/wiki/Effect_of_Hurricane_Katrina_on_New_Orleans')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://www.cnn.com/SPECIALS/2005/katrina/\" class=l onmousedown=\"return rwt(this,'','','res','3','AFQjCNG5trM8CxPUUae5QvSdwA7EXJkkCw','&amp;sig2=l0icF6xBFlPUSc4Abk2q_A')\">CNN: <b>Hurricane Katrina</b></a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>Ongoing news coverage with glossary, map, photo galleries and background material.<br><span class=a>www.cnn.com/SPECIALS/2005/katrina/ - 33k - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:f9AUd9ZNQvsJ:www.cnn.com/SPECIALS/2005/katrina/+%22hurricane+katrina%22&hl=en&ct=clnk&cd=3&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:www.cnn.com/SPECIALS/2005/katrina/\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://www.cnn.com/SPECIALS/2005/katrina/')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><h2 class=r><a href=\"http://news.google.com/news?q=%22hurricane+katrina%22&amp;num=15&amp;hl=en&amp;um=1&amp;sa=X&amp;oi=news_group&amp;resnum=4&amp;ct=title\">News results for <b>&quot;hurricane katrina&quot;</b></a></h2><br><table border=0 cellpadding=0 cellspacing=0><tr><td align=center valign=top width=76 style=\"padding:5px 10px 0 0\"><a href=\"/url?q=http://www.cbc.ca/arts/tv/story/2007/06/05/peabody-spike.html&amp;sa=X&amp;oi=news_result&amp;resnum=4&amp;ct=image&amp;usg=AFQjCNEsa5I4umqjrOMw8jilMlWWQ6ZP9g\"><img src=\"http://news.google.com/news?q=%22hurricane+katrina%22&amp;num=15&amp;um=1&amp;imgefp=E8xYef7IfysJ&amp;imgurl=www.cbc.ca/gfx/images/arts/photos/2006/08/17/spikelee-cp-10562138.jpg\" alt=\"\" border=1 width=76 height=60><br><font size=-2>CBC.ca</font></a></td><td valign=top style=\"padding-top:3px;width:40em\"><font size=-1><div><!--m--><a href=\"http://www.rte.ie/arts/2007/0605/lees.html\" class=l onmousedown=\"return rwt(this,'','','res','4','AFQjCNFT64eown4Q--TOwDQcqJWNv-7kLQ','&amp;sig2=nxf2vG8iMOkeS88pVNNveQ')\">Spike Lee to follow up Katrina film</a> - <nobr><span class=f>12 hours ago</span></nobr><br><table cellpadding=0 cellspacing=0 border=0><tr><td class=j><font size=-1>American film director Spike Lee has said that he will make a film to follow on from his award-winning documentary about the aftermath of <b>Hurricane Katrina</b>. <b>...</b></font></td></tr></table><nobr><span class=a>RTE.ie</span> - <a class=fl href=\"http://news.google.com/news?q=%22hurricane+katrina%22&amp;num=15&amp;um=1&amp;ncl=1116918159&amp;sa=X&amp;oi=news_result&amp;resnum=4&amp;ct=more-results&amp;cd=1\">56 related articles&nbsp;&raquo;</a></nobr><br><a href=\"/url?q=http://www.foxnews.com/story/0,2933,277412,00.html&amp;sa=X&amp;oi=news_result&amp;resnum=4&amp;ct=result&amp;cd=1&amp;usg=AFQjCNEdZ1mk2uJRc0kz7iJQGcd5MwinaQ\">Health Officials: <b>Hurricane Katrina</b> Still Killing</a> - <nobr><span class=a>FOX News</span> - <a class=fl href=\"http://news.google.com/news?q=%22hurricane+katrina%22&amp;num=15&amp;um=1&amp;ncl=1116866873&amp;sa=X&amp;oi=news_result&amp;resnum=4&amp;ct=more-results&amp;cd=2\">108 related articles&nbsp;&raquo;</a></nobr><br><a href=\"/url?q=http://www.signonsandiego.com/news/nation/20070604-0702-frenchmarket.html&amp;sa=X&amp;oi=news_result&amp;resnum=4&amp;ct=result&amp;cd=2&amp;usg=AFQjCNEKeR68Ke54_OSe0xKSZiNHqJwsNA\">New Orleans vendors displaced by Katrina return to French Market <b>...</b></a> - <nobr><span class=a>San Diego Union Tribune</span> - <a class=fl href=\"http://news.google.com/news?q=%22hurricane+katrina%22&amp;num=15&amp;um=1&amp;ncl=1116940550&amp;sa=X&amp;oi=news_result&amp;resnum=4&amp;ct=more-results&amp;cd=3\">101 related articles&nbsp;&raquo;</a></nobr><!--n--></div></font></div></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://news.bbc.co.uk/1/hi/in_depth/americas/2005/hurricane_katrina/default.stm\" class=l onmousedown=\"return rwt(this,'','','res','5','AFQjCNG-4CqzpY_xbvhURxIG2TjJtgCFuw','&amp;sig2=N1vQabZCxOlgPasofcHhYQ')\">BBC NEWS | Special Reports | <b>Hurricane Katrina</b></a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>Visit BBC News for up-to-the-minute news, breaking news, video, audio and feature stories. BBC News provides trusted World and UK news as well as local and <b>...</b><br><span class=a>news.bbc.co.uk/1/hi/in_depth/<wbr>americas/2005/hurricane_katrina/default.stm - 57k - Jun 3, 2007 - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:dgH1XO7QvacJ:news.bbc.co.uk/1/hi/in_depth/americas/2005/hurricane_katrina/default.stm+%22hurricane+katrina%22&hl=en&ct=clnk&cd=5&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:news.bbc.co.uk/1/hi/in_depth/americas/2005/hurricane_katrina/default.stm\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://news.bbc.co.uk/1/hi/in_depth/americas/2005/hurricane_katrina/default.stm')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://www.hurricane-katrina.org/\" class=l onmousedown=\"return rwt(this,'','','res','6','AFQjCNHOJjpDyjjIByB2nfLSsYS1A73JLw','&amp;sig2=XLJu5cNwcUZKYmNxTAckPQ')\">Beyond Katrina</a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>check out the <b>Hurricane Katrina</b> swicki at eurekster.com <b>.....</b> From disaster monitoring after <b>Hurricane Katrina</b> and the Indonesian tsunami to global crop <b>...</b><br><span class=a>www.<b>hurricane-katrina</b>.org/ - Jun 5, 2007 - </span><nobr><a class=fl href=\"/search?hl=en&q=related:www.hurricane-katrina.org/\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://www.hurricane-katrina.org/')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://www.katrina.com/\" class=l onmousedown=\"return rwt(this,'','','res','7','AFQjCNEC8RHwG2izH7D98WyAx02DlCDT4g','&amp;sig2=QuJ792hLOqMxljo78hJUBQ')\"><b>Hurricane Katrina</b> ... www.katrina.com ... by katrina blankenship <b>...</b></a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>Web design and computer consultant provides her website to assist storm victims. Includes help with locating relatives, missing persons, and relief links.<br><span class=a>www.katrina.com/ - 30k - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:gvNUDLTauHIJ:www.katrina.com/+%22hurricane+katrina%22&hl=en&ct=clnk&cd=7&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:www.katrina.com/\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://www.katrina.com/')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://www.usa.gov/Citizen/Topics/PublicSafety/Hurricane_Katrina_Recovery.shtml\" class=l onmousedown=\"return rwt(this,'','','res','8','AFQjCNE5rhZsUsVGfwoN0-PHr8lgCS2bKg','&amp;sig2=-BAh0M7avjkVw1BsGiBZ1A')\"><b>Hurricane Katrina</b> Recovery on USA.gov</a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>USA.gov: Hurricane Recovery -- Emergency relief and disaster assistance for survivors of Hurricanes Katrina, Rita and Wilma, and resources to locate family <b>...</b><br><span class=a>www.usa.gov/Citizen/Topics/PublicSafety/<wbr>Hurricane_Katrina_Recovery.shtml - 33k - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:aItbgYqTtHMJ:www.usa.gov/Citizen/Topics/PublicSafety/Hurricane_Katrina_Recovery.shtml+%22hurricane+katrina%22&hl=en&ct=clnk&cd=8&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:www.usa.gov/Citizen/Topics/PublicSafety/Hurricane_Katrina_Recovery.shtml\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://www.usa.gov/Citizen/Topics/PublicSafety/Hurricane_Katrina_Recovery.shtml')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://www.ncdc.noaa.gov/oa/climate/research/2005/katrina.html\" class=l onmousedown=\"return rwt(this,'','','res','9','AFQjCNH2etxSq8a6w_HkwhMW8jyLj25nKg','&amp;sig2=KSfi4hbqHyieC6pl71VjZA')\">NCDC: Climate of 2005: <b>Hurricane Katrina</b></a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1><b>Hurricane Katrina</b> was one of the strongest storms to impact the coast of the United States during the last 100 years. With sustained winds during landfall <b>...</b><br><span class=a>www.ncdc.noaa.gov/oa/climate/research/2005/katrina.html - 27k - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:4XNgmCzoWR8J:www.ncdc.noaa.gov/oa/climate/research/2005/katrina.html+%22hurricane+katrina%22&hl=en&ct=clnk&cd=9&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:www.ncdc.noaa.gov/oa/climate/research/2005/katrina.html\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://www.ncdc.noaa.gov/oa/climate/research/2005/katrina.html')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://katrina.louisiana.gov/\" class=l onmousedown=\"return rwt(this,'','','res','10','AFQjCNFZacJ49H9jV8n1YsaWAqSZgghs4w','&amp;sig2=CgiKa2LiIX9eV7EKXyZsuQ')\">Louisiana.gov - <b>Hurricane Katrina</b></a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>Official website covering the latest information and resources regarding relief and assistance provided by the state government for citizens impacted by <b>...</b><br><span class=a>katrina.louisiana.gov/ - 10k - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:m6sshb7fnnEJ:katrina.louisiana.gov/+%22hurricane+katrina%22&hl=en&ct=clnk&cd=10&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:katrina.louisiana.gov/\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://katrina.louisiana.gov/')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://www.msnbc.msn.com/id/9107338/\" class=l onmousedown=\"return rwt(this,'','','res','11','AFQjCNFwiCXR7ys2feP-5HflWwhwfwc-mw','&amp;sig2=WxJP0H_a12S_uD4dawIZDQ')\">MSNBC: <b>Hurricane Katrina</b> and New Orleans Recovery</a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>Full coverage of Hurricanes Katrina, Rita and Wilma, including slide shows, video galleries and the latest news.<br><span class=a>www.msnbc.msn.com/id/9107338/ - 58k - Jun 4, 2007 - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:ROxNyNw7S_4J:www.msnbc.msn.com/id/9107338/+%22hurricane+katrina%22&hl=en&ct=clnk&cd=11&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:www.msnbc.msn.com/id/9107338/\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://www.msnbc.msn.com/id/9107338/')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://news.yahoo.com/fc/US/Hurricane_Katrina\" class=l onmousedown=\"return rwt(this,'','','res','12','AFQjCNH-uYVSGFCRNKF2UBvm9D2s5QtPaA','&amp;sig2=ftL9rpuLxRMwoF7r2k6ptQ')\"><b>Hurricane Katrina</b> news from Yahoo! News</a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>Full news coverage and opinions on <b>Hurricane Katrina</b> and its aftermath. Discover ways to help the relief effort or receive aid. Find Katrina photos, audio, <b>...</b><br><span class=a>news.yahoo.com/fc/US/Hurricane_Katrina - 39k - Jun 3, 2007 - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:briDGm_5LPMJ:news.yahoo.com/fc/US/Hurricane_Katrina+%22hurricane+katrina%22&hl=en&ct=clnk&cd=12&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:news.yahoo.com/fc/US/Hurricane_Katrina\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://news.yahoo.com/fc/US/Hurricane_Katrina')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://www.nytimes.com/pages/national/nationalspecial/index.html\" class=l onmousedown=\"return rwt(this,'','','res','13','AFQjCNEqLHHeutI5OxfBhD5PcLnJAK_H0w','&amp;sig2=7DGR4Z4MwwqA2CfDH6-ioQ')\"><b>Hurricane Katrina</b> - The New York Times</a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>Find breaking news, multimedia and opinion on the aftermath of <b>Hurricane Katrina</b>.<br><span class=a>www.nytimes.com/pages/<wbr>national/nationalspecial/index.html - Jun 3, 2007 - </span><nobr><a class=fl href=\"/search?hl=en&q=related:www.nytimes.com/pages/national/nationalspecial/index.html\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://www.nytimes.com/pages/national/nationalspecial/index.html')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://www.nola.com/katrina/\" class=l onmousedown=\"return rwt(this,'','','res','14','AFQjCNF9GwHoaFOIMwTcN4hZBk-RWtx5Hg','&amp;sig2=PBTjcE51BVbGE5chn9cJqA')\">NOLA.com: <b>Hurricane Katrina</b> Archive</a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1><b>Hurricane Katrina</b> struck the New Orleans area early morning August 29, 2005. The storm surge breached the city&#39;s levees at multiple points, <b>...</b><br><span class=a>www.nola.com/katrina/ - 55k - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:6mnnH8jSzEoJ:www.nola.com/katrina/+%22hurricane+katrina%22&hl=en&ct=clnk&cd=14&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:www.nola.com/katrina/\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://www.nola.com/katrina/')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <div class=g><!--m--><h2 class=r><a href=\"http://www.hhs.gov/katrina/\" class=l onmousedown=\"return rwt(this,'','','res','15','AFQjCNEW7LXLaKmbWZZdoF5rXRzmNfCf8A','&amp;sig2=O44wh3v-XqFc42B_YzN7zA')\">Disasters and Emergencies: 2005 Hurricane Season</a></h2><table border=0 cellpadding=0 cellspacing=0><tr><td class=\"j\"><font size=-1>KatrinaRecovery.gov &middot; 2006 Hurricane Season &middot; Fact Sheet: the One Year Anniversary of <b>Hurricane Katrina</b> (White House); Continuing Progress: A 1-Year Update <b>...</b><br><span class=a>www.hhs.gov/katrina/ - 18k - </span><nobr><a class=fl href=\"http://64.233.167.104/search?q=cache:x0c1ZLYE3rwJ:www.hhs.gov/katrina/+%22hurricane+katrina%22&hl=en&ct=clnk&cd=15&gl=us\">Cached</a> - <a class=fl href=\"/search?hl=en&q=related:www.hhs.gov/katrina/\">Similar pages</a><span class=bl> - <a class=fl2 href=# onclick=\"return gnb._add(this, 'http://www.hhs.gov/katrina/')\">Note this</a></span></nobr></font><!--n--></td></tr></table></div> <!--z--></div><br><h2 class=r>Searches related to:<b> &quot;hurricane katrina&quot;</b></h2><table border=0 cellpadding=0 cellspacing=0 style=\"margin-top:6px\"><tr style=\"font-size:84%\"><td style=\"padding:0 30px 6px 0\" valign=top><a href=\"/search?q=hurricane+katrina+date&revid=1463595348&sa=X&oi=revisions_inline&resnum=1&ct=broad-revision&cd=1\"><b>hurricane katrina date</b></a></td><td>&nbsp;</td><td style=\"padding:0 30px 6px 0\" valign=top><a href=\"/search?q=hurricane+katrina+pictures&revid=1463595348&sa=X&oi=revisions_inline&resnum=1&ct=broad-revision&cd=2\"><b>hurricane katrina pictures</b></a></td><td>&nbsp;</td><td style=\"padding:0 30px 6px 0\" valign=top><a href=\"/search?q=hurricane+rita&revid=1463595348&sa=X&oi=revisions_inline&resnum=1&ct=broad-revision&cd=3\"><b>hurricane rita</b></a></td><td>&nbsp;</td><td style=\"padding:0 30px 6px 0\" valign=top><a href=\"/search?q=hurricane+katrina+articles&revid=1463595348&sa=X&oi=revisions_inline&resnum=1&ct=broad-revision&cd=4\"><b>hurricane katrina articles</b></a></td><td>&nbsp;</td></tr><tr style=\"font-size:84%\"><td style=\"padding:0 30px 6px 0\" valign=top><a href=\"/search?q=hurricane+andrew&revid=1463595348&sa=X&oi=revisions_inline&resnum=1&ct=broad-revision&cd=5\"><b>hurricane andrew</b></a></td><td>&nbsp;</td><td style=\"padding:0 30px 6px 0\" valign=top><a href=\"/search?q=hurricane+katrina+video&revid=1463595348&sa=X&oi=revisions_inline&resnum=1&ct=broad-revision&cd=6\"><b>hurricane katrina video</b></a></td><td>&nbsp;</td><td style=\"padding:0 30px 6px 0\" valign=top><a href=\"/search?q=hurricane+wilma&revid=1463595348&sa=X&oi=revisions_inline&resnum=1&ct=broad-revision&cd=7\"><b>hurricane wilma</b></a></td><td>&nbsp;</td><td style=\"padding:0 30px 6px 0\" valign=top><a href=\"/search?q=hurricane+katrina+timeline&revid=1463595348&sa=X&oi=revisions_inline&resnum=1&ct=broad-revision&cd=8\"><b>hurricane katrina timeline</b></a></td><td>&nbsp;</td></tr></table><br><br clear=all><div id=navbar class=n><table border=0 cellpadding=0 width=1% cellspacing=0 align=center><tr align=center style=text-align:center valign=top><td nowrap><div id=nf></div><td nowrap><div id=nc></div><span class=i>1</span><td nowrap><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=15&sa=N><div class=nr></div>2</a><td nowrap><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=30&sa=N><div class=nr></div>3</a><td nowrap><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=45&sa=N><div class=nr></div>4</a><td nowrap><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=60&sa=N><div class=nr></div>5</a><td nowrap><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=75&sa=N><div class=nr></div>6</a><td nowrap><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=90&sa=N><div class=nr></div>7</a><td nowrap><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=105&sa=N><div class=nr></div>8</a><td nowrap><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=120&sa=N><div class=nr></div>9</a><td nowrap><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=135&sa=N><div class=nr></div>10</a><td nowrap class=b><a href=/search?q=%22hurricane+katrina%22&num=15&hl=en&start=15&sa=N><div id=nn></div>Next</a></table></div></div><center>\n\n<br clear=all><table cellpadding=\"10\"><tr><td nowrap=\"nowrap\" bgcolor=\"#e5ecf9\"><font size=\"-1\">Download <a href=/url?sa=L&ai=BECzVRedlRvzpE6WciwGb9pneBbn40R6p__agA7WBmbIP0IYDEAEYASDBVDgAUNT7qocBYMkGmAGulwM&num=1&q=http://pack.google.com/%3Futm_source%3Den_US-rpp-holiday_06%26utm_medium%3Drpp%26utm_campaign%3Den_US>Google Pack</a>: free essential software for your PC</font></td></tr></table><br><table border=0 cellpadding=0 cellspacing=0 width=100% class=\"ft t bb bt\"><tr><td align=center>&nbsp;<br><table border=0 cellpadding=0 cellspacing=0 align=center><form method=GET action=/search><tr><td nowrap>\n<font size=-1><input type=text name=q size=31 maxlength=2048 value=\"&quot;hurricane katrina&quot;\" title=\"Search\"> <input type=submit name=btnG value=\"Search\"><input type=hidden name=num value=15><input type=hidden name=hl value=\"en\"></font></td></tr></form></table><br><font size=-1><a href=/swr?q=%22hurricane+katrina%22&hl=en&swrnum=5760000>Search&nbsp;within&nbsp;results</a> | <a href=/language_tools?q=%22hurricane+katrina%22&hl=en>Language Tools</a> | <a href=\"/intl/en/help.html\">Search&nbsp;Tips</a> | <a href=\"/quality_form?q=%22hurricane+katrina%22&amp;num=15&amp;hl=en\" target=_blank>Dissatisfied? Help us improve</a></font><br><br></td></tr></table></center><center><p><hr class=z><div style=\"padding:2px\" class=\"ft\"><font size=-1>&copy;2007 Google - <a href=\"/\">Google&nbsp;Home</a> - <a href=\"/intl/en/ads/\">Advertising&nbsp;Programs</a> - <a href=\"/services/\">Business Solutions</a> - <a href=\"/intl/en/about.html\">About Google</a></font></div><br></center></body></html>\n";
   
   static final String BAD_STUFF =
	   "<script>document.getElementById('gbarc').style.height=(document.getElementById('gbari').height=window.gbar.getPad())+'px';document.getElementById('gbar').innerHTML=window.gbar.getHtml([['w','Web','',1],['i','Images','http://images.google.com/images?um=1',1],['v','Video','http://video.google.com/videosearch?um=1',1],['n','News','http://news.google.com/news?um=1',1],['l','Maps','http://maps.google.com/maps?um=1',1],['m','Gmail','http://mail.google.com/mail?um=1',1],['','more','#',3],['b','Blog Search','http://blogsearch.google.com/blogsearch?um=1',2],['j','Blogger','http://www.blogger.com/?um=1',2],['p','Books','http://books.google.com/books?um=1',2],['c','Calendar','http://www.google.com/calendar?um=1',2],['o','Documents','http://docs.google.com/?um=1',2],['e','Finance','http://finance.google.com/finance?um=1',2],['g','Groups','http://groups.google.com/groups?um=1',2],['z','Labs','http://labs.google.com/',2],['0','Orkut','http://www.orkut.com/?um=1',2],['t','Patents','http://www.google.com/patents?um=1',2],['q','Photos','http://picasaweb.google.com/lh/searchbrowse?um=1&psc=G',2],['f','Products','http://www.google.com/products?um=1',2],['y','Reader','http://www.google.com/reader?um=1',2],['s','Scholar','http://scholar.google.com/scholar?um=1',2]]);</script><a href=\"http://yo.com\">yo</a>";
   
   public static void main(String[] args)
   {
      Backend backend = new Backend()
      {
		 HashMap attributesMap = new HashMap();
		 public void meta(HashMap<String, String> attributesMap)
		 {
		 }
		 public void newBody(HashMap<String, String> attributesMap)
		 {
		 }
		 public void newFrame(HashMap<String, String> attributesMap)
		 {
		 }
		 public void newIFrame(HashMap<String, String> attributesMap)
		 {
		 }
		 public void newAHref(HashMap<String, String> attributesMap)
		 {
			println("newAHref("+attributesString(attributesMap));
		 }
		 public void  newImageMapArea(HashMap<String, String> attributesMap)
		 {
		 }
		 public void closeHref()
		 {
		 }
		 public void newTextToken(String s, String delim)
		 {
			println("newTextToken("+s);
		 }
		 public void newMinedHref(String urlString)
		 {
		 }
		 public void newMinedImg(String urlString)
		 {
		 }
		 public void newWhitespace()
		 {
		 }
		 public void closeBlock(String tag, HashMap<String, String> attributesMap)
		 {
			println("closeBlock(<"+tag+attributesString(attributesMap));
		 }
		 public void startTitle()
		 {
			 println("title(<"+attributesString(attributesMap));
		 }
		 public void closeTitle()
		 {
		 }
		 public void  newImg(HashMap<String, String> attributesMap)
		 {
			println("newImg("+attributesString(attributesMap));
		 }
		 public void setBold(boolean on)
		 {
		 }
		 public void setItalic(boolean on)
		 {
		 }
		 public void streamClosed()
		 {
		 }
		 public void  setContainer(String urlString)
		 {
			println("setContainer("+ urlString + " !!!!");
		 }
		 public void fontTag(HashMap<String, String> attributesMap) 
		 {
		 }
      };

      try
      {
		 InputStream is;
		 
		 if (args.length == 0)
		 {
			is = new StringBufferInputStream(BAD_STUFF);
		 }
		 else
		 {
			ParsedURL purl	= ParsedURL.getAbsolute(args[0], "main init()");

			URLConnection connection		= purl.url().openConnection();
			// hack so google thinks we're a normal browser
			// (otherwise, it wont serve us)
			connection.setRequestProperty("user-agent", A_USER_AGENT);
			is = connection.getInputStream();
		 }
		 Parser parser = new Parser(is, backend);
		 parser.parse();
      } catch (Exception e)
      {
		 e.printStackTrace();
      }
   }
   final static String A_USER_AGENT	= "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0";

   public static HashMap<String, String> getAttributesMap()
   {
      Thread currentThread = Thread.currentThread();
      HashMap<String, String> attributesMap	= attributesMapMap.get(currentThread);
	  if (attributesMap == null)
	  {
		 attributesMap = new HashMap<String, String>(20);
		 attributesMapMap.put(currentThread, attributesMap);
	  }
      return attributesMap;
   }
   
   public static String attributesString(HashMap<String, String> attributesMap)
   {
	   StringBuilder buffy	= new StringBuilder();

	   Iterator <Map.Entry<String,String>> iterator = attributesMap.entrySet().iterator();

	   while (iterator.hasNext())
	   {
		   Map.Entry<String,String> entry	= iterator.next();
		   String key	= entry.getKey();
		   String value	= entry.getValue();
		   buffy.append(" ").append(key).append('=').append('"').append(value).append('"');
	   }
	   
	   return buffy.toString();
   }
}

