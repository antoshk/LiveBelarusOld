/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.live_belarus;

/**
 *
 * @author User
 */
public class EndPoints {
    public static String MediaByLoc = "https://www.instagram.com/explore/locations/[loc_id]/?__a=1";
    public static String MediaByUsername = "https://www.instagram.com/[username]/?__a=1";
    public static String LocIdByLatLng = "https://api.instagram.com/v1/locations/search?access_token=1486469032.f3b208c.7755f38ed7804eaca06ac11a3827e192&lat=[lat]&lng=[lng]";
    public static String RevGeocode = "https://maps.googleapis.com/maps/api/geocode/json?latlng=[lat],[lng]&key=AIzaSyDHM52jBgWoaZA8-NUfqAU24DzBY_mV4sM&language=ru";
    public static String MediaByMediaCode = "https://www.instagram.com/p/[mediaCode]/?__a=1";
    public static String FollowersByUsername = "http://localhost/getFriends.php?username=[username]";
    public static String TagsByTagname = "http://www.instagram.com/explore/tags/[tagname]/?__a=1";
    public static String CustomSearch = "https://www.googleapis.com/customsearch/v1?key=AIzaSyBmerlyydx3nc98V94mD0n6wbDkHTQNbIQ&cx=018076421826403416814:nq-97xkzp2c&q=";
    /*public static String UserInfoByUserId = "https://www.instagram.com/query/?q=ig_user([userId]){id,username,external_url,full_name,profile_pic_url,biography,"+
            "followed_by{count},follows{count},media{count},is_private,is_verified}";
    public static String UserFollowersFirst50 = "https://www.instagram.com/query/?q=ig_user([userId]){followed_by.first(50){followed_by.first(50){page_info,nodes"+
            "{username,id,biography,is_private,followed_by{count},follows{count},media{count}}}}";
    public static String UserFollowersNext50 = "https://www.instagram.com/query/?q=ig_user([userId]){followed_by.after([startCursor],50){followed_by.first(50){page_info,nodes"+
            "{username,id,biography,is_private,followed_by{count},follows{count},media{count}}}}";
    public static String LocationByMediaCode = "https://www.instagram.com/query/?q=ig_shortcode([mediaCode]){code,location{id,lat,lng,name}}";
    public static String FastMediaCaptionByUserId = "https://www.instagram.com/query/?q=ig_user([userId]){media.first(5){page_info,nodes{id,caption,date}}}";*/
    
    
    //https://www.instagram.com/query/?q=ig_hashtag($hashtag)
    //ig_location(637976185) { media.after(1266515888681876449, 12) {count,nodes {caption,code,comments {count},date,dimensions {height,width},display_src,id,is_video,likes {count},owner {id},thumbnail_src,video_views},page_info}}
}