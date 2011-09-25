$(document).ready(function () {

    jQuery.fn.center = function() {
        this.css("position", "absolute");
        this.css("top", ($(window).height() - this.outerHeight()) / 2 + $(window).scrollTop() + "px");
        this.css("left", ($(window).width() - this.outerWidth()) / 2 + $(window).scrollLeft() + "px");
        return this;
    };

    $('.Modal').modalpop({ speed: 300 });

});

/*
 ModalPop

 Author: Owain Lewis
 Author URL: www.Owainlewis.com
 Simple Modal Dialog for jQuery
 The idea here was to keep this plugin as lightweight and easy to customize as possible
 You are free to use this plugin for whatever you want.
 If you enjoy this plugin, I'd love to hear from you

 With minor edits to the way the popup is centered based on
 http://stackoverflow.com/questions/210717/what-is-the-best-way-to-center-a-div-on-the-screen-using-jquery
 */

(function () {
    jQuery.fn.modalpop = function (options) {

        var defaults = {
            speed: 500,
            center: false
        };

        var options = $.extend(defaults, options);
        var width = $(window).width();
        //Get the full page height including the scroll area
        var height = $(document).height();
        jQuery('body').prepend("<div id='mask'></div>");
        jQuery('#mask').css('height', height);
        jQuery('#mask').css('width', width);

        return this.each(function () {

            jQuery(this).click(function () {
                $this = jQuery(this);
                var id = $this.attr('href');
                var commentable_id = $(id).attr('data-commentable-id');
                var commentable_name = $(id).attr('data-commentable-name');
                var cloudimage = $('<img>', {
                    src : 'http://localhost:5000/d2f/Disorganizer?commentable=' + commentable_id,
                    alt : commentable_name + ' Cloud',
                    title : commentable_name + ' Cloud'
                });
                cloudimage.load(function() {
                    $(id).center();
                });
                $(id).html(cloudimage);
                $('#mask').css('filter', 'alpha(opacity=80)');
                jQuery('#mask').fadeIn(defaults.speed);
                jQuery(id).fadeIn(defaults.speed);
                return false;
            });

            jQuery('#mask').click(function () {
                jQuery(this).hide();
                jQuery('.cloud_popup').hide();
            });

        });
    };

})(jQuery);