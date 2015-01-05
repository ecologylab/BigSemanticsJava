package ecologylab.bigsemantics.html;

/**
 * HTML Tags ordered by Function (http://www.w3schools.com/tags/ref_byfunc.asp)
 * 
 * Basic Tags
 * <html>, <body>, <h1>to<h6>, <p>, <br>, <hr>
 * 
 * Char Format
 * <b>, <font>, <i>, <em>, <big>, <strong>, <small>, <sup>, <sub>, <bdo>, <u>
 * 
 * Output
 * <pre>, <code>, <tt>, <kbd>, <var>, <dfn>, <samp>, <xmp> 
 * 
 * Blocks
 * <acronym>, <abbr>, <address>, <blockquote>, <center>, <q>, <cite>, <ins>, <del>,
 * <s>, <strike>
 * 
 * Links
 * <a>, <link>
 * 
 * Frames
 * <frame>, <frameset>, <noframes>, <iframe>
 * 
 * Input
 * <form>, <input>, <textarea>, <button>, <select>, <optgroup>, <option>, <label>,
 * <fieldset>, <legend>, <isindex>
 * 
 * Lists
 * <ul>, <ol>, <li>, <dir>, <dl>, <dt>, <dd>, <menu>
 * 
 * Images
 * <img>, <map>, <area>
 * 
 * Tables
 * <table>, <caption>, <th>, <tr>, <td>, <thead>, <tbody>, <tfoot>, <col>, <colgroup>
 * 
 * Styles
 * <style>, <div>, <span>
 * 
 * Meta Info
 * <head>, <title>, <meta>, <base>, <basefont>
 * 
 * Programming
 * <script>, <noscript>, <applet>, <object>, <param>
 * 
 * @author eunyee
 */
public interface HTMLTags
{
	final static String blockTags[] = { 
		"body", "h1", "h2", "h3", "h4", "h5", "h6", "p", "pre",  
		"blockquote", "table", "tr", "td", "tbody", "caption", "div", "span"
	};
	
	final static String linkTags[] = { "a", "link" };
	
	final static String imgTags[] = { "img" };
	
}