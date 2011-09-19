(function($) {
    $.ajaxSetup({
        cache: true,
        beforeSend: function(xhr) {
            $('#loading').show();
            xhr.setRequestHeader('Accept', 'text/javascript' )
        },
        complete: function(xhr, status) {
            $('#loading').hide();
        }
    });
    
    $.fn.submitWithAjax = function() {
        $(this).submit(function() {
            $.post($(this).attr('action'), $(this).serialize(), null, 'script');
            return false;
        });
    };
    
    $.fn.selectSort = function() {
        var order = $.cookie('order') || '(likes - dislikes) DESC, created_at ASC';
        return $(this).find('select').val(order);
    };
    
    $.fn.processReady = function() {
        $(this).find('select[name=order]').change(function() {
            $.cookie('order', $(this).val());
            $.getScript($(this).parents('tr').prev().prev().find('.view_comments').attr('href'));
        });
        
        $(this).find('a.thumbs_up, a.thumbs_down').unbind('click').click(function() {
            if ($(this).hasClass('active') == false) {
                $(this).addClass('active');
                var option = $(this).hasClass('thumbs_up') ? 1 : 0
                $.post($(this).attr('href'), {
                    'vote[option]': option
                }, null, 'script');
            } else {
                alert('You already voted');
            }
            return false;
        });
        
        $(this).find('.view_comments').unbind('click').click(function() {
            if ($(this).html() == 'Hide Comments') {
                $(this).html('View Comments (' + $(this).attr('data-count') + ')');
                $(this).parents('tr').next().next().hide();
            } else {
                $.getScript($(this).attr('href'));
            }
            return false;
        });
        
        $(this).find('.ajax a').unbind('click').click(function() {
            $.getScript($(this).attr('href'));
            return false;
        });
    };
    
    $(document).ready(function() { 
        $('html').processReady();
    });
})(jQuery);